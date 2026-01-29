package com.example.moamoa_backend.auth.dto.oauth;

import com.example.moamoa_backend.auth.exception.AuthException;
import com.example.moamoa_backend.auth.exception.code.AuthErrorCode;
import com.example.moamoa_backend.member.entity.Member;
import com.example.moamoa_backend.member.enums.MemberStatus;
import com.example.moamoa_backend.member.enums.Provider;
import com.example.moamoa_backend.member.enums.Role;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * OAuth2 Provider별 응답을 통일된 포맷으로 변환하는 DTO
 */
@Getter
public class OAuthAttributes {

    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private Provider provider;
    private String providerId;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey,
                           String name, String email, Provider provider, String providerId) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.provider = provider;
        this.providerId = providerId;
    }

    /**
     * Provider별 팩토리 메서드
     */
    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("kakao".equals(registrationId)) {
            return ofKakao(userNameAttributeName, attributes);
        }
        if("google".equals(registrationId)){
            return ofGoogle(userNameAttributeName, attributes);
        }
        throw new AuthException(AuthErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        GoogleUserInfo googleUserInfo = new GoogleUserInfo(attributes);

        return OAuthAttributes.builder()
                .name(googleUserInfo.getName())
                .email(googleUserInfo.getEmail())
                .provider(Provider.GOOGLE)
                .providerId(googleUserInfo.getProviderId())
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }
    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(attributes);

        return OAuthAttributes.builder()
                .name(kakaoUserInfo.getName())
                .email(kakaoUserInfo.getEmail())
                .provider(Provider.KAKAO)
                .providerId(kakaoUserInfo.getProviderId())
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    /**
     * 신규 회원용 Member 엔티티 생성
     */
    public Member toEntity() {
        return Member.builder()
                .email(email)
                .name(name)
                .provider(provider)
                .providerId(providerId)
                .role(Role.ROLE_GUEST)
                .status(MemberStatus.ACTIVE)
                .build();
    }
}