package com.example.moamoa_backend.global.security.jwt;

import com.example.moamoa_backend.global.security.jwt.exception.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 * - 모든 HTTP 요청에 대해 JWT 토큰의 유효성을 검사하는 필터
 * - OncePerRequestFilter를 상속받아 요청당 한 번만 실행됨
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    /**
     * 필터 로직 수행
     * 1. Authorization 헤더에서 토큰 추출
     * 2. 토큰 유효성 및 타입(Access Token) 검증
     * 3. 검증 성공 시 Authentication 객체를 생성하여 SecurityContext에 저장
     * 4. 검증 실패 시 예외 코드를 request 속성에 담고 다음 필터로 진행 (EntryPoint에서 처리)
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = null;
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 1. Authorization 헤더 확인 및 "Bearer " 접두사 제거
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // 2. 토큰이 없으면 검증을 건너뛰고 다음 필터로 진행
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 3. 토큰 유효성 검증 (위조, 만료 등 검사)
            jwtUtil.validateToken(token);

            // 4. 토큰 타입 검증
            jwtUtil.validateTokenType(token, "access_token");

            // 5. 검증 성공: 사용자 인증 객체 생성 및 SecurityContext에 저장
            Authentication authentication = jwtUtil.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (JwtException e) {
            // 6. JWT 관련 예외 발생 시 처리
            // 이후 SecurityConfig에 등록된 AuthenticationEntryPoint가 이 값을 꺼내서 클라이언트에게 JSON 응답을 보냄.
            request.setAttribute("exception", e.getCode());
        }

        // 7. 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}