package com.example.moamoa_backend.domain.inquiry.dto;

import com.example.moamoa_backend.domain.inquiry.enums.InquiryCategory;

import java.time.LocalDateTime;
import java.util.List;

public class InquiryDetailResDto {
    public record MyInquiryDetail(
            Long inquiryId,
            InquiryCategory category,
            String title,
            String content,
            boolean answered,
            LocalDateTime createdAt,
            LocalDateTime answeredAt,
            String answerContent,         // 없으면 null 가능

            List<String> inquiryImageUrls,
            List<String> answerImageUrls
    ) {}
}
