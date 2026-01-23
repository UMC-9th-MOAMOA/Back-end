package com.example.moamoa_backend.auth.service;

import com.example.moamoa_backend.auth.dto.oauth.OAuthAttributes;
import com.example.moamoa_backend.member.entity.Member;
import com.example.moamoa_backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * 소셜 로그인 후처리를 담당하는 Service
 * - Provider(Google, Naver 등)로부터 받은 사용자 정보를 기반으로
 * - 강제 회원가입(Insert) 또는 기존 회원 조회(Select)를 수행
 * - SecurityContext에 저장될 OAuth2User 객체를 반환
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("Processing OAuth2 login for registrationId: {}", userRequest.getClientRegistration().getRegistrationId());

        // 1. Provider(Google 등)로부터 User Info 가져오기 (Default 구현체 위임)
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 2. OAuth2 Provider 식별자 및 PK 키값 추출
        // registrationId: google, naver, kakao ...
        // userNameAttributeName: sub(Google), response(Naver), id(Kakao) ...
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        // 3. 표준화된 DTO로 변환 (Provider별 상이한 JSON 구조 통일)
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        // 4. 사용자 영속화 (신규 가입 or 기존 회원 조회)
        Member member = saveOrUpdate(attributes);

        // 5. Principal 반환 (SecurityContext 저장용)
        // Role 정보와 함께 반환하여 이후 권한 제어(Authorize)에 사용됨
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getRole().name())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey()
        );
    }

    /**
     * 사용자 정보 동기화 정책
     * - 기존 회원: 별도 Update 없이 조회된 엔티티 반환 (사용자 커스텀 정보 유지)
     * - 신규 회원: Provider 정보를 기반으로 DB Insert
     */
    private Member saveOrUpdate(OAuthAttributes attributes) {
        Member member = memberRepository.findByEmailAndProvider(attributes.getEmail(), attributes.getProvider())
                .orElse(null);

        if (member != null) {
            return member;
        }

        return memberRepository.save(attributes.toEntity());
    }
}