package com.example.moamoa_backend.domain.inquiry.service.command;

import com.example.moamoa_backend.global.service.S3UploadService;
import com.example.moamoa_backend.domain.inquiry.converter.InquiryAnswerConverter;
import com.example.moamoa_backend.domain.inquiry.converter.InquiryConverter;
import com.example.moamoa_backend.domain.inquiry.dto.InquiryAnswerRequestDto;
import com.example.moamoa_backend.domain.inquiry.dto.InquiryAnswerResponseDto;
import com.example.moamoa_backend.domain.inquiry.dto.InquiryRequestDTO;
import com.example.moamoa_backend.domain.inquiry.dto.InquiryResponseDTO;
import com.example.moamoa_backend.domain.inquiry.entity.AnswerImage;
import com.example.moamoa_backend.domain.inquiry.entity.Inquiry;
import com.example.moamoa_backend.domain.inquiry.enums.InquiryStatus;
import com.example.moamoa_backend.domain.inquiry.exception.InquiryException;
import com.example.moamoa_backend.domain.inquiry.exception.code.InquiryErrorCode;
import com.example.moamoa_backend.domain.inquiry.repository.InquiryRepository;
import com.example.moamoa_backend.domain.member.entity.Member;
import com.example.moamoa_backend.domain.member.exception.MemberException;
import com.example.moamoa_backend.domain.member.exception.code.MemberErrorCode;
import com.example.moamoa_backend.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
/**
 * 문의 생성/답변 등록을 처리하는 Command 서비스.
 *
 * 공통 정책
 * - 이미지 최대 5장 제한
 * - 이미지 업로드 실패 시 도메인 예외로 변환
 */
public class InquiryCommandServiceImpl implements InquiryCommandService {

	private final InquiryRepository inquiryRepository;
	private final MemberRepository memberRepository;
	private final S3UploadService s3UploadService;

	@Override
	/**
	 * 문의를 생성한다.
	 *
	 * 처리 흐름
	 * 1) 회원 존재 확인
	 * 2) (선택) 이미지 업로드 -> URL 리스트 생성 (최대 5장)
	 * 3) Inquiry 엔티티 생성 후 저장
	 *
	 * @param memberId 문의를 생성할 회원 ID
	 * @param request 문의 생성 요청 DTO
	 * @param images 첨부 이미지 파일 목록(선택)
	 * @return 문의 생성 결과 DTO
	 */
	public InquiryResponseDTO.CreateResult create(Long memberId, InquiryRequestDTO.Create request,
		List<MultipartFile> images) {

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

		// 1) 이미지 파일 -> S3 업로드 -> URL 리스트
		List<String> uploadedUrls = new ArrayList<>();
		if (images != null && !images.isEmpty()) {
			if (images.size() > 5) {
				throw new InquiryException(InquiryErrorCode.TOO_MANY_IMAGES);
			}

			for (MultipartFile file : images) {
				if (file == null || file.isEmpty())
					continue;
				try {
					String url = s3UploadService.upload(file, "inquiries");
					if (url != null)
						uploadedUrls.add(url);
				} catch (IOException e) {
					throw new InquiryException(InquiryErrorCode.IMAGE_UPLOAD_FAILED);
				}
			}
		}

		// 2) DB 저장 (Inquiry + InquiryImage)
		Inquiry inquiry = InquiryConverter.toEntity(member, request, uploadedUrls);
		Inquiry saved = inquiryRepository.save(inquiry);

		return InquiryConverter.toCreateResult(saved);
	}

	@Override
	/**
	 * 문의에 답변을 등록한다.
	 *
	 * 처리 흐름
	 * 1) 문의 존재 확인
	 * 2) 답변 내용/응답자/시간/상태 변경
	 * 3) (선택) 답변 이미지 업로드 -> AnswerImage로 매핑 (최대 5장)
	 * 4) 저장 후 응답 DTO 반환
	 *
	 * 교체 정책
	 * - 기존 answerImages 컬렉션을 clear 후 새로 채움
	 *
	 * @param inquiryId 답변할 문의 ID
	 * @param request 답변 요청 DTO
	 * @param images 답변 이미지 파일 목록(선택)
	 * @return 답변 등록 결과 DTO
	 */
	public InquiryAnswerResponseDto.CreateResult answer(Long inquiryId, InquiryAnswerRequestDto.CreateAnswer request,
		List<MultipartFile> images) {

		Inquiry inquiry = inquiryRepository.findById(inquiryId)
			.orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));

		inquiry.setAnswer(request.answer());
		inquiry.setResponderName(request.responderName());
		inquiry.setAnsweredAt(LocalDateTime.now());
		inquiry.setStatus(InquiryStatus.COMPLETED);

		// 컬렉션 null 방지
		if (inquiry.getAnswerImages() == null) {
			inquiry.setAnswerImages(new ArrayList<>());
		}
		// 교체 정책
		inquiry.getAnswerImages().clear();

		// 1) 답변 이미지 파일 -> S3 업로드 -> AnswerImage 저장
		List<String> uploadedUrls = new ArrayList<>();
		if (images != null && !images.isEmpty()) {
			if (images.size() > 5) {
				throw new InquiryException(InquiryErrorCode.TOO_MANY_ANSWER_IMAGES);
			}
			int sortOrder = 1;

			for (MultipartFile file : images) {
				if (file == null || file.isEmpty())
					continue;

				try {
					String url = s3UploadService.upload(file, "inquiries/answers");
					if (url == null)
						continue;

					uploadedUrls.add(url);

					AnswerImage img = AnswerImage.builder()
						.imageUrl(url)
						.sortOrder(sortOrder++)
						.build();

					img.setInquiry(inquiry);
					inquiry.getAnswerImages().add(img);

				} catch (IOException e) {
					throw new InquiryException(InquiryErrorCode.IMAGE_UPLOAD_FAILED);
				}
			}
		}

		inquiryRepository.save(inquiry);

		// 기존 컨버터 그대로 활용(응답 형태 유지)
		return InquiryAnswerConverter.toCreateResult(inquiry, uploadedUrls);
	}
}
