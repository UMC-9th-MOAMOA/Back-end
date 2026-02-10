package com.example.moamoa_backend.auth.dto.res;

import lombok.Builder;

/**
 * 인증 관련 Response DTO
 */
public class AuthResDto {

    /**
     * 클라이언트 응답용 (Refresh Token 제외)
     */
    @Builder
    public record TokenDto(
            String grantType,
            String accessToken,
            Long accessTokenExpiresIn
    ) {}

    /**
     * 내부 전달용 (Refresh Token 포함)
     */
    @Builder
    public record GeneratedTokenDto(
            String grantType,
            String accessToken,
            String refreshToken,
            Long accessTokenExpiresIn
    ) {}

    /**
     * 비밀번호 변경 토큰
     * - 이메일 인증 성공시 발급되며 비밀번호 초기화에 사용
     */
    public record PasswordResetTokenDto(
            String resetToken
    ) {}

    /**
     * 로그인 응답 (내부 전달용)
     * - 온보딩 여부와 정책 동의 여부를 포함해 프론트 라우팅에 사용
     */
    @Builder
    public record LoginResultDto(
            GeneratedTokenDto generatedToken,
            Boolean onboardingCompleted,
            Boolean policyAgreed
    ) {}

    /**
     * 로그인 응답 (외부 전달용)
     * - 온보딩 여부와 정책 동의 여부를 포함해 프론트 라우팅에 사용
     */
    @Builder
    public record LoginResponseDto(
            TokenDto token,
            Boolean onboardingCompleted,
            Boolean policyAgreed
    ) {}

}