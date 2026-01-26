package com.example.moamoa_backend.policy.dto.res;


import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 정책 관련 Response DTO
 */
public class PolicyResDto {

    @Builder
    public record DetailDto(
            Long id,
            String policyType,
            boolean isMandatory,
            String version,
            String title,
            String content,
            boolean isActive,
            LocalDateTime effectiveAt
    ) {}
}
