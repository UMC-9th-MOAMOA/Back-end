package com.example.moamoa_backend.domain.inquiry.dto;

import java.time.LocalDateTime;

public class InquiryResponseDTO {

    public record CreateResult(
            Long inquiryId,
            LocalDateTime createdAt
    ) {}

}
