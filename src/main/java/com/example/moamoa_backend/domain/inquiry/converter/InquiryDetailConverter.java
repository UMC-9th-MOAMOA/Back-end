package com.example.moamoa_backend.domain.inquiry.converter;

import com.example.moamoa_backend.domain.inquiry.dto.InquiryDetailResDto;
import com.example.moamoa_backend.domain.inquiry.entity.Inquiry;

import java.util.List;

public class InquiryDetailConverter {

    public static InquiryDetailResDto.MyInquiryDetail toMyInquiryDetail(
            Inquiry inquiry,
            List<String> inquiryImageUrls,
            List<String> answerImageUrls
    ) {
        boolean answered = inquiry.getAnsweredAt() != null;

        return new InquiryDetailResDto.MyInquiryDetail(
                inquiry.getId(),
                inquiry.getCategory(),
                inquiry.getTitle(),
                inquiry.getContent(),
                answered,
                inquiry.getCreatedAt(),
                inquiry.getAnsweredAt(),
                inquiry.getAnswer(), // 답변 내용(없으면 null)
                inquiryImageUrls,
                answerImageUrls
        );
    }
}
