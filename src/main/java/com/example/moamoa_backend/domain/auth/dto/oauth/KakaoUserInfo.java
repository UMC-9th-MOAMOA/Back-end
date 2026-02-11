package com.example.moamoa_backend.domain.auth.dto.oauth;

import java.util.Collections;
import java.util.Map;

/**
 * Kakao OAuth2 사용자 정보 구현체
 */
public class KakaoUserInfo implements OAuth2UserInfo {

	private final Map<String, Object> attributes;
	private final Map<String, Object> kakaoAccount;
	private final Map<String, Object> profile;

	@SuppressWarnings("unchecked")
	public KakaoUserInfo(Map<String, Object> attributes) {
		this.attributes = attributes;

		Object accountObj = attributes.get("kakao_account");
		this.kakaoAccount = accountObj instanceof Map
			? (Map<String, Object>)accountObj
			: Collections.emptyMap();

		Object profileObj = kakaoAccount.get("profile");
		this.profile = profileObj instanceof Map
			? (Map<String, Object>)profileObj
			: Collections.emptyMap();
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public String getProviderId() {
		Object id = attributes.get("id");
		if (id == null)
			throw new IllegalArgumentException("Kakao OAuth response missing required id");

		return String.valueOf(id);
	}

	@Override
	public String getProvider() {
		return "kakao";
	}

	@Override
	public String getEmail() {
		return (String)kakaoAccount.get("email");
	}

	@Override
	public String getName() {
		return (String)profile.get("nickname");
	}
}