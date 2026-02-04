package com.example.moamoa_backend.inquiry.service.command;


import com.example.moamoa_backend.global.service.S3UploadService;
import com.example.moamoa_backend.inquiry.converter.InquiryAnswerConverter;
import com.example.moamoa_backend.inquiry.converter.InquiryConverter;
import com.example.moamoa_backend.inquiry.dto.InquiryAnswerRequestDto;
import com.example.moamoa_backend.inquiry.dto.InquiryAnswerResponseDto;
import com.example.moamoa_backend.inquiry.dto.InquiryRequestDTO;
import com.example.moamoa_backend.inquiry.dto.InquiryResponseDTO;
import com.example.moamoa_backend.inquiry.entity.AnswerImage;
import com.example.moamoa_backend.inquiry.entity.Inquiry;
import com.example.moamoa_backend.inquiry.enums.InquiryStatus;
import com.example.moamoa_backend.inquiry.exception.InquiryException;
import com.example.moamoa_backend.inquiry.exception.code.InquiryErrorCode;
import com.example.moamoa_backend.inquiry.repository.InquiryRepository;
import com.example.moamoa_backend.member.entity.Member;
import com.example.moamoa_backend.member.exception.MemberException;
import com.example.moamoa_backend.member.exception.code.MemberErrorCode;
import com.example.moamoa_backend.member.repository.MemberRepository;
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
public class InquiryCommandServiceImpl implements InquiryCommandService {

    private final InquiryRepository inquiryRepository;
    private final MemberRepository memberRepository;
    private final S3UploadService s3UploadService;

    @Override
    public InquiryResponseDTO.CreateResult create(Long memberId, InquiryRequestDTO.Create request, List<MultipartFile> images) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 1) 이미지 파일 -> S3 업로드 -> URL 리스트
        List<String> uploadedUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            if (images.size() > 5) {
                throw new InquiryException(InquiryErrorCode.TOO_MANY_IMAGES);
            }

            for (MultipartFile file : images) {
                if (file == null || file.isEmpty()) continue;
                try {
                    String url = s3UploadService.upload(file, "inquiries");
                    if (url != null) uploadedUrls.add(url);
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
    public InquiryAnswerResponseDto.CreateResult answer(Long inquiryId, InquiryAnswerRequestDto.CreateAnswer request, List<MultipartFile> images) {

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        inquiry.setAnswer(request.answer());
        inquiry.setAnsweredAt(LocalDateTime.now());
        inquiry.setStatus(InquiryStatus.COMPLETED);

        // ✅ 컬렉션 null 방지
        if (inquiry.getAnswerImages() == null) {
            inquiry.setAnswerImages(new ArrayList<>());
        }
        // ✅ 교체 정책
        inquiry.getAnswerImages().clear();

        // 1) 답변 이미지 파일 -> S3 업로드 -> AnswerImage 저장
        List<String> uploadedUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
             if (images.size() > 5) {
                 throw new InquiryException(InquiryErrorCode.TOO_MANY_ANSWER_IMAGES);
             }
            int sortOrder = 1;

            for (MultipartFile file : images) {
                if (file == null || file.isEmpty()) continue;

                try {
                    String url = s3UploadService.upload(file, "inquiries/answers");
                    if (url == null) continue;

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