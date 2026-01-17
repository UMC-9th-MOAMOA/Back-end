package com.example.moamoa_backend.inquiry.service.command;


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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryCommandServiceImpl implements InquiryCommandService{

    private final InquiryRepository inquiryRepository;
    private final MemberRepository memberRepository;

    @Override
    public InquiryResponseDTO.CreateResult create(Long memberId, InquiryRequestDTO.Create request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        Inquiry inquiry = InquiryConverter.toEntity(member, request);
        Inquiry saved = inquiryRepository.save(inquiry);

        return InquiryConverter.toCreateResult(saved);
    }

    @Override
    public InquiryAnswerResponseDto.CreateResult answer(Long inquiryId, InquiryAnswerRequestDto.CreateAnswer request) {

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        inquiry.setAnswer(request.answer());
        inquiry.setAnsweredAt(LocalDateTime.now());
        inquiry.setStatus(InquiryStatus.COMPLETED);

        // ✅ 컬렉션 null 방지 (엔티티에서 초기화가 정석이지만, 안전하게 2중 방어)
        if (inquiry.getAnswerImages() == null) {
            inquiry.setAnswerImages(new ArrayList<>()); // setter가 있어야 함
        }

        // ✅ 교체 정책이면 clear (orphanRemoval=true면 기존 row 삭제)
        inquiry.getAnswerImages().clear();

        List<String> urls = request.answerImageUrls();
        if (urls != null && !urls.isEmpty()) {
            int sortOrder = 0;
            for (String url : urls) {
                AnswerImage img = AnswerImage.builder()
                        .imageUrl(url)
                        .sortOrder(sortOrder++)
                        .build();
                img.setInquiry(inquiry);
                inquiry.getAnswerImages().add(img);
            }
        }

        inquiryRepository.save(inquiry);

        return new InquiryAnswerResponseDto.CreateResult(
                inquiry.getId(),
                inquiry.getAnswer(),
                inquiry.getAnsweredAt(),
                urls == null ? List.of() : urls
        );
    }
}
