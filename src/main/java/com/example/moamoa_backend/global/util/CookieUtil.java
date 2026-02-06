package com.example.moamoa_backend.global.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * HTTP 쿠키 생성 및 관리 유틸리티 클래스
 */
@Component
public class CookieUtil {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String COOKIE_PATH = "/";
    private static final String SAME_SITE_POLICY = "None";

    private final int refreshTokenMaxAge;

    public CookieUtil(@Value("${jwt.refresh-token-validity}") long refreshTokenValidity) {
        // 밀리초를 초로 변환
        this.refreshTokenMaxAge = (int) (refreshTokenValidity / 1000);
    }

    /**
     * Refresh Token 쿠키를 생성하여 응답에 추가합니다.
     *
     * @param response HttpServletResponse
     * @param refreshToken Refresh Token 값
     */
    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .path(COOKIE_PATH)
                .sameSite(SAME_SITE_POLICY)
                .httpOnly(true)
                .secure(true)
                .maxAge(refreshTokenMaxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * Refresh Token 쿠키를 삭제합니다.
     *
     * @param response HttpServletResponse
     */
    public void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .path(COOKIE_PATH)
                .sameSite(SAME_SITE_POLICY)
                .httpOnly(true)
                .secure(true)
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}