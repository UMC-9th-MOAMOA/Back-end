package com.example.moamoa_backend.domain.auth.dto.oauth;

import java.util.Map;

/**
 * OAuth2 Provider별 사용자 정보 통일 인터페이스
 */
public interface OAuth2UserInfo {

	String getProviderId();

	String getProvider();

	String getEmail();

	String getName();

	@Deprecated(forRemoval = true)
	Map<String, Object> getAttributes(); // 전달받은 원본 데이터
}