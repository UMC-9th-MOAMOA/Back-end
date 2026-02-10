package com.example.moamoa_backend.domain.inquiry.dto;

import com.example.moamoa_backend.domain.inquiry.enums.InquiryCategory;

import java.time.LocalDateTime;
import java.util.List;

public class InquiryQueryResDto {

    public record Cursor(
            LocalDateTime createdAt,
            Long id
    ) {}

    public record MyInquiryItem(
            Long inquiryId,
            InquiryCategory category,
            String title,
            String contentPreview,
            boolean answered,              // 답변완료 여부
            LocalDateTime createdAt,
            String responderName,
            String answerPreview
    ) {}

    public record MyInquiryList(
            List<MyInquiryItem> items,
            boolean hasNext,
            Cursor nextCursor
    ) {}
}
