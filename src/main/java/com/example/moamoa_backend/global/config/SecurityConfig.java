package com.example.moamoa_backend.global.config;

import com.example.moamoa_backend.global.security.jwt.JwtAuthFilter;
import com.example.moamoa_backend.global.security.jwt.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private final String[] allowUris = {
            "/api/v1/auth/signup",
            "/api/v1/auth/login",
            "/api/v1/auth/email/send-verification",
            "/api/v1/auth/email/verify",
            "/actuator/health/**",
            "/api/v1/missions/admin"
    };

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

                //CORS 설정 연결
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // URL별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**").permitAll() //swagger 관련 경로
                        .requestMatchers(allowUris).permitAll() //추가로 명시하는 경로

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint));


        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 프론트엔드 주소 허용 (localhost:3000 등)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",       // 로컬 테스트용
                "http://3.38.228.147:3000",    // EC2 배포 (포트 포함)
                "http://3.38.228.147"          // EC2 배포 (포트 생략/Nginx 사용 시)
        ));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 모든 헤더 허용
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 쿠키/인증 정보 포함 허용
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Encoder Bean 등록
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}