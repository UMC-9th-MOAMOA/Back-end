package com.example.moamoa_backend.domain.policy.dto.res;

import lombok.Builder;

/**
 * 정책 관련 Response DTO
 */
public class PolicyResDto {

    /**
     * 회원가입 약관 상세 조회
     */
    @Builder
    public record DetailDto(
            Long id,
            String title,
            String content,
            boolean isMandatory
    ) {}

    /**
     * 회원가입 약관 단순 조회
     */
    @Builder
    public record SimpleDto(
            Long id,
            String title,
            boolean isMandatory
    ) {}
}
