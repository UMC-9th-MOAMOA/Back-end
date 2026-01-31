package com.example.moamoa_backend.global.security.filter;

import com.example.moamoa_backend.auth.exception.code.AuthErrorCode;
import com.example.moamoa_backend.member.enums.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * 정책 동의 여부 검증 필터
 *
 * 소셜 로그인 후 아직 정책(이용약관, 개인정보처리방침 등)에 동의하지 않은
 * GUEST 사용자의 API 접근을 제한한다.
 *
 * ExceptionTranslationFilter 뒤에 배치하여 AccessDeniedException을
 * AccessDeniedHandler가 처리할 수 있도록 함
 */
public class PolicyAgreementFilter extends OncePerRequestFilter {

    private final String[] guestAllowUris;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Security Config에서 관리
    public PolicyAgreementFilter(String[] guestAllowUris) {
        this.guestAllowUris = guestAllowUris;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // GUEST도 접근 가능한 경로는 검증 없이 통과 (정책 동의 API 등)
        if (isGuestWhitelisted(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            boolean isGuest = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(Role.ROLE_GUEST.name()));

            // GUEST는 정책 동의 전까지 일반 API 접근 불가
            if (isGuest) {
                request.setAttribute("exception", AuthErrorCode.POLICY_NOT_AGREED);
                throw new AccessDeniedException("Policy not agreed");
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isGuestWhitelisted(String path) {
        return Arrays.stream(guestAllowUris)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}