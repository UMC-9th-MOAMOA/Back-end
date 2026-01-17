package com.example.moamoa_backend.inquiry.converter;

import com.example.moamoa_backend.inquiry.dto.InquiryAnswerResponseDto;
import com.example.moamoa_backend.inquiry.entity.AnswerImage;
import com.example.moamoa_backend.inquiry.entity.Inquiry;

import java.util.List;

public class InquiryAnswerConverter {
    public static List<AnswerImage> toAnswerImages(List<String> urls) {
        if (urls == null || urls.isEmpty()) return List.of();

        return urls.stream()
                .map(url -> AnswerImage.builder()
                        .imageUrl(url) // ✅ 컬럼명에 맞게
                        .build())
                .toList();
    }

    public static InquiryAnswerResponseDto.CreateResult toCreateResult(Inquiry inquiry, List<String> urls) {
        return new InquiryAnswerResponseDto.CreateResult(
                inquiry.getId(),
                inquiry.getAnswer(),
                inquiry.getAnsweredAt(),
                urls == null ? List.of() : urls
        );
    }
}
