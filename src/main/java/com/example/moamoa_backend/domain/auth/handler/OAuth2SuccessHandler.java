package com.example.moamoa_backend.domain.auth.handler;

import com.example.moamoa_backend.domain.auth.exception.AuthException;
import com.example.moamoa_backend.domain.auth.exception.code.AuthErrorCode;
import com.example.moamoa_backend.global.security.jwt.JwtUtil;
import com.example.moamoa_backend.global.util.RedisUtil;
import com.example.moamoa_backend.domain.member.entity.Member;
import com.example.moamoa_backend.domain.member.enums.MemberStatus;
import com.example.moamoa_backend.domain.member.enums.Provider;
import com.example.moamoa_backend.domain.member.exception.MemberException;
import com.example.moamoa_backend.domain.member.exception.code.MemberErrorCode;
import com.example.moamoa_backend.domain.member.repository.MemberRepository;
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
import java.util.UUID;

/**
 * OAuth2 인증 성공 핸들러
 * - CustomOAuth2UserService 처리 완료 후 호출
 * - JWT 발급 및 프론트엔드 리다이렉트 담당
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

    @Value("${jwt.refresh-token-validity}")
    private Long refreshTokenValidityMs;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        // Provider 정보 추출
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = authToken.getAuthorizedClientRegistrationId();
        Provider provider;
        try {
            provider = Provider.valueOf(registrationId.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AuthException(AuthErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
        }

        String providerId = (String) attributes.get("providerId");

        // 회원 조회
        Member member = memberRepository.findByProviderAndProviderId(provider, providerId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        // BANNED 회원 차단
        if (member.getStatus() == MemberStatus.BANNED) {
            String errorUrl = frontUrl + "/oauth/callback?error=ACCOUNT_BANNED";
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
            return;
        }

        // 임시 코드 생성 및 Redis 저장 (30초 유효)
        String authCode = UUID.randomUUID().toString();
        redisUtil.setDataExpire("OAUTH_CODE:" + authCode, String.valueOf(member.getId()), 30);

        // 프론트엔드로 리다이렉트 (임시 코드 전달)
        String targetUrl = UriComponentsBuilder.fromUriString(frontUrl + "/oauth/callback")
                .queryParam("code", authCode)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

}