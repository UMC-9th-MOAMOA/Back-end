package com.example.moamoa_backend.global.security.filter;

import com.example.moamoa_backend.member.enums.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;


public class PolicyAgreementFilter extends OncePerRequestFilter {

    private final String[] guestAllowUris;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public PolicyAgreementFilter(String[] guestAllowUris) {
        this.guestAllowUris = guestAllowUris;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 화이트리스트 경로는 통과
        if (isGuestWhitelisted(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 인증 정보 확인
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            boolean isGuest = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(Role.ROLE_GUEST.name()));

            if (isGuest) {
                sendErrorResponse(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isGuestWhitelisted(String path) {
        return Arrays.stream(guestAllowUris)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private void sendErrorResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");

        String jsonResponse = """
                {
                    "isSuccess": false,
                    "code": "AUTH403_5",
                    "message": "아직 정책에 동의하지 않은 회원입니다. 정책 동의 후 이용해주세요.",
                    "result": null
                }
                """;

        response.getWriter().write(jsonResponse);
    }
}