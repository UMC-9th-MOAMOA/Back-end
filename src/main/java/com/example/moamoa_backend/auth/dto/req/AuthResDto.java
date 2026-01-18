package com.example.moamoa_backend.auth.dto.req;

import lombok.Builder;

public class AuthResDto {

    // 로그인, Reissue 요청시 응답
    @Builder
    public record TokenDto(
            String grantType, //Bearer
            String accessToken,
            String refreshToken,
            Long accessTokenExpiresIn //access 만료시간 정보
    ) {}

}
