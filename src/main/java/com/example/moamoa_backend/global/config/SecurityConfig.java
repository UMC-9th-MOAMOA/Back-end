package com.example.moamoa_backend.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Creates and exposes a BCryptPasswordEncoder bean used for hashing user passwords.
     *
     * @return a BCryptPasswordEncoder instance for encoding passwords
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private final String[] allowUris = {
            "/api/v1/auth/signup",
            "/api/v1/auth/login"
    };

    /**
     * Configure and return the application's SecurityFilterChain for a stateless, token-based security setup.
     *
     * The chain disables CSRF, the default form login, and HTTP Basic auth; enforces stateless session management;
     * permits access to Swagger/OpenAPI endpoints and the URIs listed in {@code allowUris}; and requires authentication for all other requests.
     *
     * @return the configured SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화
                .csrf(AbstractHttpConfigurer::disable)

                // 기본 로그인 폼 및 HTTP Basic 인증 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // JWT 사용으로 stateless 설정
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // URL별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**").permitAll() //swagger 관련 경로
                        .requestMatchers(allowUris).permitAll() //추가로 명시하는 경로

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                );


        return http.build();
    }
}