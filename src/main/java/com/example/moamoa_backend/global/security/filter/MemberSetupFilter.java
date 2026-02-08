package com.example.moamoa_backend.global.security.filter;

import com.example.moamoa_backend.auth.exception.code.AuthErrorCode;
import com.example.moamoa_backend.member.enums.Role;
import com.example.moamoa_backend.member.repository.MemberRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 회원 상태 검증 필터
 *
 * 인증된 사용자의 정책 동의 여부 및 온보딩 완료 여부를 검증하여
 * 미완료 시 API 접근을 제한한다.
 *
 * TODO: 성능 최적화 - 현재 매 요청마다 DB 조회 발생
 *       JWT Claims에 policyAgreed, onboardingCompleted 포함하는 방식으로 개선 예정
 *       (토큰 발급 시 1회 조회 → 이후 DB 조회 없음)
 */
public class MemberSetupFilter extends OncePerRequestFilter {

    private final List<RequestMatcher> setupAllowMatchers;
	private final MemberRepository memberRepository;

	// Security Config에서 관리
	public MemberSetupFilter(
            List<RequestMatcher> setupAllowMatchers,
            MemberRepository memberRepository
    ) {
        this.setupAllowMatchers = setupAllowMatchers;
        this.memberRepository = memberRepository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 인증되지 않은 요청은 통과
        if (auth == null || !auth.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean isUser = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.ROLE_USER.name()));

        // USER 아닌경우 통과
        if (!isUser) {
            filterChain.doFilter(request, response);
            return;
        }

        // Setup Whitelist는 통과
        if (isSetupWhitelisted(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        Long memberId = Long.parseLong(auth.getName());

        // 정책 동의 여부 체크
        if (!memberRepository.existsByIdAndPolicyAgreedTrue(memberId)) {
            request.setAttribute("exception", AuthErrorCode.POLICY_NOT_AGREED);
            throw new AccessDeniedException("Policy not agreed");
        }

        // 온보딩 완료 여부 체크
        if (!memberRepository.existsByIdAndOnboardingCompletedTrue(memberId)) {
            request.setAttribute("exception", AuthErrorCode.ONBOARDING_NOT_COMPLETED);
            throw new AccessDeniedException("Onboarding not completed");
        }

		filterChain.doFilter(request, response);
	}

    private boolean isSetupWhitelisted(HttpServletRequest request) {
        return setupAllowMatchers.stream()
                .anyMatch(matcher -> matcher.matches(request));
    }

}