package com.example.moamoa_backend.auth.dto.oauth;

import java.util.Map;

/*
 * Google 소셜 로그인으로 받은 객체를 OAuth2UserInfo 규격에 맞추어 꺼내주는 구현체
 */
public class GoogleUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public GoogleUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub"); // 구글의 식별자 키 sub
    }

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }
}
