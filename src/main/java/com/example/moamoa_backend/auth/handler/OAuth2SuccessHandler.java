package com.example.moamoa_backend.auth.handler;

import com.example.moamoa_backend.global.security.jwt.JwtUtil;
import com.example.moamoa_backend.global.util.RedisUtil;
import com.example.moamoa_backend.member.entity.Member;
import com.example.moamoa_backend.member.enums.Provider;
import com.example.moamoa_backend.member.exception.MemberException;
import com.example.moamoa_backend.member.exception.code.MemberErrorCode;
import com.example.moamoa_backend.member.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

/**
 * OAuth2 인증 성공 시 실행되는 핸들러 (Authorization Server -> Client)
 * - CustomOAuth2UserService에서 인증이 완료된 후 호출됨
 * - JWT(Access/Refresh)를 발급하고, 이를 클라이언트(프론트엔드)에게 전달하는 역할
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final RedisUtil redisUtil;

    @Value("${app.front-url:http://localhost:3000}")
    private String frontUrl;

    // yml에서 설정값 가져오기 (밀리초 단위)
    @Value("${jwt.refresh-token-validity}")
    private Long refreshTokenValidityMs;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        // 1. 인증 객체로부터 Provider 및 사용자 정보 추출
        // CustomOAuth2UserService에서 반환한 OAuth2User 객체를 사용
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = authToken.getAuthorizedClientRegistrationId();
        Provider provider = Provider.valueOf(registrationId.toUpperCase());

        String email = (String) attributes.get("email");

        // 2. 내부 회원 정보 조회 (JWT Payload 생성 목적)
        // Service 계층에서 이미 가입/업데이트 처리가 완료되었으므로, 여기서는 식별자(ID)와 권한(Role) 획득이 주 목적
        Member member = memberRepository.findByEmailAndProvider(email, provider)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 3. JWT 토큰 발급
        // Access Token: API 요청 인가(Authorization) 용도
        // Refresh Token: Access Token 만료 시 재발급 용도
        String accessToken = jwtUtil.createAccessToken(member.getId(), String.valueOf(member.getRole()));
        String refreshToken = jwtUtil.createRefreshToken(member.getId());

        // 4. Refresh Token 서버 저장소(Redis) 저장
        // 토큰 탈취 대응(Blacklist) 및 로그아웃 처리를 위해 Redis에 저장 (Key: RT:{memberId})
        long refreshTokenExpireSeconds = refreshTokenValidityMs / 1000;
        String redisKey = "RT:" + member.getId();

        redisUtil.setDataExpire(redisKey, refreshToken, refreshTokenExpireSeconds);

        // 5. 토큰 전달 및 리다이렉트 처리
        // Refresh Token -> 보안 강화를 위해 HttpOnly 쿠키에 담아 전달
        // Access Token -> 리다이렉트 URL 쿼리 파라미터에 담아 프론트엔드로 전달
        response.addCookie(createCookie("refreshToken", refreshToken, refreshTokenExpireSeconds));

        String targetUrl = UriComponentsBuilder.fromUriString(frontUrl + "/oauth/callback")
                .queryParam("accessToken", accessToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * 쿠키 생성 유틸리티 메서드
     * - XSS 공격 방지를 위해 HttpOnly 설정 적용
     * - 주의: setSecure(true)는 HTTPS 환경에서만 쿠키가 전송됨 (로컬 테스트 시 주의 필요)
     * - 추가: 기존 구현한 토큰 생성 로직과 동일하기 때문에 이후 refactor 필요! (중보코드)
     */
    private Cookie createCookie(String key, String value, long maxAgeSeconds) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge((int) maxAgeSeconds);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        return cookie;
    }
}