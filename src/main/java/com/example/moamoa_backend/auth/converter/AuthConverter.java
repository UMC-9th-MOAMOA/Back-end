package com.example.moamoa_backend.auth.converter;

import com.example.moamoa_backend.auth.dto.res.AuthResDto;

/**
 * Auth 관련 DTO 변환 클래스
 */
public class AuthConverter {

    /**
     * 내부 DTO → 응답 DTO 변환 (Refresh Token 제외)
     */
    public static AuthResDto.TokenDto toTokenDto(AuthResDto.GeneratedTokenDto generatedDto) {
        return AuthResDto.TokenDto.builder()
                .grantType(generatedDto.grantType())
                .accessToken(generatedDto.accessToken())
                .accessTokenExpiresIn(generatedDto.accessTokenExpiresIn())
                .build();
    }
}