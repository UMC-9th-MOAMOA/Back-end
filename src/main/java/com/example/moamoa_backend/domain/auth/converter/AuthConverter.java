package com.example.moamoa_backend.auth.converter;

import com.example.moamoa_backend.auth.dto.res.AuthResDto;

/**
 * Auth 관련 DTO 변환 클래스
 */
public class AuthConverter {

    /**
     * 토큰 응답 변환
     * 내부 DTO → 응답 DTO 변환 (Refresh Token 제외)
     */
    public static AuthResDto.TokenDto toTokenDto(AuthResDto.GeneratedTokenDto generatedDto) {
        return AuthResDto.TokenDto.builder()
                .grantType(generatedDto.grantType())
                .accessToken(generatedDto.accessToken())
                .accessTokenExpiresIn(generatedDto.accessTokenExpiresIn())
                .build();
    }

    /**
     * 로그인 응답 변환
     * 내부 DTO → 응답 DTO 변환 (Refresh Token 제외)
     */
    public static AuthResDto.LoginResponseDto toLoginResponseDto(AuthResDto.LoginResultDto loginResultDto) {
        return AuthResDto.LoginResponseDto.builder()
                .token(toTokenDto(loginResultDto.generatedToken()))
                .onboardingCompleted(loginResultDto.onboardingCompleted())
                .policyAgreed(loginResultDto.policyAgreed())
                .build();
    }
}