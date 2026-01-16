package com.example.moamoa_backend.inquiry.converter;

import com.example.moamoa_backend.inquiry.dto.InquiryRequestDTO;
import com.example.moamoa_backend.inquiry.dto.InquiryResponseDTO;
import com.example.moamoa_backend.inquiry.entity.Inquiry;
import com.example.moamoa_backend.inquiry.entity.InquiryImage;
import com.example.moamoa_backend.inquiry.enums.InquiryStatus;
import com.example.moamoa_backend.member.entity.Member;

import java.util.ArrayList;
import java.util.List;

public class InquiryConverter {

    private InquiryConverter() {}

    public static Inquiry toEntity(Member member, InquiryRequestDTO.Create request) {

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

        List<String> urls = request.imageUrls();
        if (urls != null && !urls.isEmpty()) {
            List<InquiryImage> images = new ArrayList<>();

            for (int i = 0; i < urls.size(); i++) {
                InquiryImage img = InquiryImage.builder()
                        .imageUrl(urls.get(i))
                        .sortOrder(i + 1)
                        .build();

                // ✅ FK 주인에 inquiry 세팅 (setter 사용)
                img.setInquiry(inquiry);

                images.add(img);
            }

            // ✅ 역방향 컬렉션도 채워두면 조회 시 편함
            inquiry.getInquiryImages().addAll(images);
        }

        return inquiry;
    }

    public static InquiryResponseDTO.CreateResult toCreateResult(Inquiry inquiry) {
        return new InquiryResponseDTO.CreateResult(inquiry.getId(), inquiry.getCreatedAt());
    }

}
