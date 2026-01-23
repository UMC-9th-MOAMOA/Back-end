package com.example.moamoa_backend.auth.dto.oauth;

import com.example.moamoa_backend.member.entity.Member;
import com.example.moamoa_backend.member.enums.MemberStatus;
import com.example.moamoa_backend.member.enums.Provider;
import com.example.moamoa_backend.member.enums.Role;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * 각 소셜 공급자(Google, Naver, Kakao 등)마다 상이한 응답 JSON 구조를
 * 통일된 포맷으로 변환(Adapter)하여 관리하기 위한 DTO 클래스
 */
@Getter
public class OAuthAttributes {

    private Map<String, Object> attributes; // OAuth2 반환하는 유저 정보 Map (Raw Data)
    private String nameAttributeKey;        // 소셜 로그인 PK 기준이 되는 키 (sub, response, id 등)
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
     * OAuth2User에서 반환하는 사용자 정보는 Map이기 때문에 값 하나하나를 변환해야 함.
     * 소셜 타입(registrationId)을 식별하여 적절한 메서드(ofGoogle, ofKakao 등)를 호출하는 팩토리 메서드
     */
    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        // 이후 카카오 소셜 로그인 추가 시 여기서 분기처리 예정
        return ofGoogle(userNameAttributeName, attributes);
    }

    /**
     * Google 로그인 데이터를 OAuthAttributes 객체로 변환
     */
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

    /**
     * [신규 회원가입 시 실행]
     * UserInfo 정보를 바탕으로 Member 엔티티를 생성 (Insert 목적)
     * - 가입 시점의 기본 권한(ROLE_USER)과 상태(ACTIVE)를 설정함
     */
    public Member toEntity() {
        return Member.builder()
                .email(email)
                .name(name)
                .provider(provider)
                .providerId(providerId)
                .role(Role.ROLE_USER)
                .status(MemberStatus.ACTIVE)
                .build();
    }
}