package com.example.moamoa_backend.inquiry.dto;

import java.time.LocalDateTime;

public class InquiryResponseDTO {

    public record CreateResult(
            Long inquiryId,
            LocalDateTime createdAt
    ) {}

}
