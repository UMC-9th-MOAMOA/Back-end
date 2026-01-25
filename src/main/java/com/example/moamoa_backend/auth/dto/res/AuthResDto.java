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

}