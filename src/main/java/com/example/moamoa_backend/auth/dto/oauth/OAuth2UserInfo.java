package com.example.moamoa_backend.auth.dto.oauth;

import java.util.Map;

/*
 * 구글, 카카오 등 소셜 로그인별 JSON 응답이 모두 동일하지 않기 때문에, 이를 통일하기 위한 공통 인터페이스
 */
public interface OAuth2UserInfo {
    Map<String, Object> getAttributes(); // 소셜에서 받은 원본 데이터
    String getProviderId();              // 소셜 식별자 (Google: sub, Kakao: id)
    String getProvider();                // google, kakao, naver
    String getEmail();                   // 메일주소
    String getName();                    // 이름(서비스상 닉네임으로 사용)
}
