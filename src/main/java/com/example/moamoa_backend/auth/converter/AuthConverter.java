package com.example.moamoa_backend.auth.converter;

import com.example.moamoa_backend.auth.dto.res.AuthResDto;

/**
 * Auth 관련 DTO 변환 클래스
 */
public class AuthConverter {

    /**
     * Converts an internal GeneratedTokenDto into an API TokenDto response, excluding any refresh token.
     *
     * @param generatedDto the internal DTO containing token details to expose in the response
     * @return the response DTO containing grant type, access token, and access token expiry information
     */
    public static AuthResDto.TokenDto toTokenDto(AuthResDto.GeneratedTokenDto generatedDto) {
        return AuthResDto.TokenDto.builder()
                .grantType(generatedDto.grantType())
                .accessToken(generatedDto.accessToken())
                .accessTokenExpiresIn(generatedDto.accessTokenExpiresIn())
                .build();
    }

    /**
     * Converts an internal login result DTO into a login response DTO for API responses.
     *
     * The resulting DTO contains the access token (excluding any refresh token) and the
     * user's onboarding and policy-agreement flags.
     *
     * @param loginResultDto the internal login result containing the generated token and user flags
     * @return an AuthResDto.LoginResponseDto with `token`, `onboardingCompleted`, and `policyAgreed` populated
     */
    public static AuthResDto.LoginResponseDto toLoginResponseDto(AuthResDto.LoginResultDto loginResultDto) {
        return AuthResDto.LoginResponseDto.builder()
                .token(toTokenDto(loginResultDto.generatedToken()))
                .onboardingCompleted(loginResultDto.onboardingCompleted())
                .policyAgreed(loginResultDto.policyAgreed())
                .build();
    }
}