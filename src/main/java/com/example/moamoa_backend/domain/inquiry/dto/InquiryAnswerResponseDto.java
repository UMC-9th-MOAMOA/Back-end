package com.example.moamoa_backend.inquiry.dto;

import java.time.LocalDateTime;
import java.util.List;

public class InquiryAnswerResponseDto {
    public record CreateResult(
            Long inquiryId,
            String answer,
            LocalDateTime answeredAt,
            List<String> answerImageUrls
    ) {}
}
