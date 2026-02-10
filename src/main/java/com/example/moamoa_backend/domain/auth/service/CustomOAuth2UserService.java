package com.example.moamoa_backend.domain.auth.service;

import com.example.moamoa_backend.domain.auth.dto.oauth.OAuthAttributes;
import com.example.moamoa_backend.domain.member.entity.Member;
import com.example.moamoa_backend.domain.member.enums.MemberStatus;
import com.example.moamoa_backend.domain.member.repository.MemberRepository;
import com.example.moamoa_backend.domain.wallet.service.command.WalletCommandService;
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
import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 사용자 정보 처리 서비스
 * - Provider별 사용자 정보를 표준화하여 처리
 * - 신규 회원 자동 가입 / 기존 회원 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;
    private final WalletCommandService walletCommandService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("Processing OAuth2 login for registrationId: {}", userRequest.getClientRegistration().getRegistrationId());

        // Provider로부터 사용자 정보 조회
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // Provider 식별 정보 추출
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        // Provider별 응답을 표준 DTO로 변환
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        // 회원 저장 또는 조회
        Member member = saveOrUpdate(attributes);

        // SuccessHandler에서 사용할 providerId 추가
        Map<String, Object> modifiedAttributes = new HashMap<>(attributes.getAttributes());
        modifiedAttributes.put("providerId", attributes.getProviderId());

        // SecurityContext용 Principal 반환
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getRole().name())),
                modifiedAttributes,
                attributes.getNameAttributeKey()
        );
    }

    /**
     * 회원 저장 또는 조회
     * - 기존 회원: 조회 (WITHDRAWN 상태면 자동 복구)
     * - 신규 회원: 저장
     */
    private Member saveOrUpdate(OAuthAttributes attributes) {
        Member member = memberRepository.findByProviderAndProviderId(attributes.getProvider(), attributes.getProviderId())
                .orElse(null);

        if (member != null) {
            // WITHDRAWN 상태 재로그인 시 자동 복구
            if(member.getStatus() == MemberStatus.WITHDRAWN){
                member.activate();
            }
            return member;
        }

        Member savedMember = memberRepository.save(attributes.toEntity());
        walletCommandService.createWallet(savedMember);

        return savedMember;
    }
}