package com.example.moamoa_backend.domain.inquiry.converter;

import com.example.moamoa_backend.domain.inquiry.dto.InquiryRequestDTO;
import com.example.moamoa_backend.domain.inquiry.dto.InquiryResponseDTO;
import com.example.moamoa_backend.domain.inquiry.entity.Inquiry;
import com.example.moamoa_backend.domain.inquiry.entity.InquiryImage;
import com.example.moamoa_backend.domain.inquiry.enums.InquiryStatus;
import com.example.moamoa_backend.domain.member.entity.Member;

import java.util.ArrayList;
import java.util.List;

public class InquiryConverter {

    private InquiryConverter() {}

    public static Inquiry toEntity(Member member, InquiryRequestDTO.Create request, List<String> imageUrls) {

        Inquiry inquiry = Inquiry.builder()
                .member(member)
                .category(request.category())
                .title(request.title())
                .content(request.content())
                .isSecret(true)                 // ✅ 항상 비공개
                .status(InquiryStatus.WAITING)  // ✅ 최초 답변대기
                .answer(null)
                .answeredAt(null)
                .build();

        if (imageUrls != null && !imageUrls.isEmpty()) {
            List<InquiryImage> images = new ArrayList<>();

            for (int i = 0; i < imageUrls.size(); i++) {
                String url = imageUrls.get(i);
                if (url == null || url.isBlank()) continue;

                InquiryImage img = InquiryImage.builder()
                        .imageUrl(url)
                        .sortOrder(i + 1)
                        .build();

                img.setInquiry(inquiry);
                images.add(img);
            }

            inquiry.getInquiryImages().addAll(images);
        }

        return inquiry;
    }

    public static InquiryResponseDTO.CreateResult toCreateResult(Inquiry inquiry) {
        return new InquiryResponseDTO.CreateResult(inquiry.getId(), inquiry.getCreatedAt());
    }
}
