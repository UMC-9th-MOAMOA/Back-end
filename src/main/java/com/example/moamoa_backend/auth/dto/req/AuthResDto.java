package com.example.moamoa_backend.auth.dto.req;

import lombok.Builder;

public class AuthResDto {

    // login, refresh 요청시 응답
    @Builder
    public record TokenDto(
            String grantType, // Bearer
            String accessToken,
            Long accessTokenExpiresIn // access 만료시간 정보
    ) {}

    // login, refresh 요청시 controller layer 까지 정보 전달
    @Builder
    public record GeneratedTokenDto(
            String grantType, //Bearer
            String accessToken,
            String refreshToken,
            Long accessTokenExpiresIn // access 만료시간 정보
    ) {}

}
