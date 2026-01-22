package com.example.moamoa_backend.auth.converter;

import com.example.moamoa_backend.auth.dto.req.AuthResDto;

public class AuthConverter {

    // 내부용 DTO -> 외부 응답용 DTO 변환 (Refresh Token 제외)
    public static AuthResDto.TokenDto toTokenDto(AuthResDto.GeneratedTokenDto generatedDto) {
        return AuthResDto.TokenDto.builder()
                .grantType(generatedDto.grantType())
                .accessToken(generatedDto.accessToken())
                .accessTokenExpiresIn(generatedDto.accessTokenExpiresIn())
                .build();
    }
}
