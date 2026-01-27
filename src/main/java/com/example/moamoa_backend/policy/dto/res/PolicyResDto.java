package com.example.moamoa_backend.policy.dto.res;


import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 정책 관련 Response DTO
 */
public class PolicyResDto {

    /**
     * 약관 상세 조회용
     */
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

    /**
     * 약관 목록 조회용 (회원가입)
     */
    @Builder
    public record SignupDto(
            Long id,
            String title,
            String content,
            boolean isMandatory
    ) {}
}
