package com.example.moamoa_backend.global.config;

import com.example.moamoa_backend.auth.handler.OAuth2SuccessHandler;
import com.example.moamoa_backend.auth.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import com.example.moamoa_backend.auth.service.CustomOAuth2UserService;
import com.example.moamoa_backend.global.security.filter.PolicyAgreementFilter;
import com.example.moamoa_backend.global.security.jwt.JwtAccessDeniedHandler;
import com.example.moamoa_backend.global.security.jwt.JwtAuthFilter;
import com.example.moamoa_backend.global.security.jwt.JwtAuthenticationEntryPoint;
import com.example.moamoa_backend.member.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 설정
 *
 * 필터 체인 순서:
 * JwtAuthFilter → ExceptionTranslationFilter → PolicyAgreementFilter → AuthorizationFilter
 *
 * 예외 처리:
 * - 인증 실패(401) → JwtAuthenticationEntryPoint
 * - 인가 실패(403) → JwtAccessDeniedHandler
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    // 로그인 필요X - 모두 접근 가능
    private final String[] allowUris = {
            // 가입, 로그인, 탈퇴복구, Refresh
            "/api/v1/auth/signup",
            "/api/v1/auth/login",
            "/api/v1/auth/recover",
            "/api/v1/auth/refresh",

            // 비밀번호 초기화
            "/api/v1/auth/password-reset/send-code",
            "/api/v1/auth/password-reset/verify",
            "/api/v1/auth/password-reset",

            // 회원가입 이메일 인증
            "/api/v1/auth/email/send-code",
            "/api/v1/auth/email/verify",

            "/api/v1/auth/oauth2/token",
            "/api/v1/auth/social/**",
            "/login/**",

            // 헬스체크
            "/actuator/health/**",

    };

    // 로그인 필요X - GET 요청만 가능
    private final String[] allowGetUris = {
            "/api/v1/policies/**"
    };

    // 로그인 필요O - GUEST도 접근 가능 (정책 동의 등)
    private final String[] guestAllowUris = {
            "/api/v1/auth/**",
            "/api/v1/policies/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**",
    };

    // 관리자만 접근 가능
    private final String[] adminUris = {
            "/api/v1/admin/**",
            "/api/v1/missions/admin"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**").permitAll();
                    auth.requestMatchers(allowUris).permitAll();
                    auth.requestMatchers(HttpMethod.GET, allowGetUris).permitAll();

                    if (adminUris.length > 0) {
                        auth.requestMatchers(adminUris).hasAuthority(Role.ROLE_ADMIN.name());
                    }

                    auth.anyRequest().authenticated();
                })

                // 필터 등록: JwtAuthFilter → (ExceptionTranslationFilter) → PolicyAgreementFilter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(policyAgreementFilter(), ExceptionTranslationFilter.class)

                // 예외 핸들러 등록
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )

                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> endpoint
                                .baseUri("/api/v1/auth/social")
                                .authorizationRequestRepository(cookieAuthorizationRequestRepository)
                        )
                        .userInfoEndpoint(endpoint -> endpoint
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "https://moamoa.io.kr",
                "https://www.moamoa.io.kr",
                "https://moamoamoa.netlify.app",
                "https://api.moamoa.io.kr",
                "http://localhost:5173",
                "https://localhost:5173"
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PolicyAgreementFilter policyAgreementFilter() {
        return new PolicyAgreementFilter(guestAllowUris);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}