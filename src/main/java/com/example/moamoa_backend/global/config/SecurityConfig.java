package com.example.moamoa_backend.global.config;

import com.example.moamoa_backend.auth.handler.OAuth2SuccessHandler;
import com.example.moamoa_backend.auth.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import com.example.moamoa_backend.auth.service.CustomOAuth2UserService;
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
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    // 로그인 필요X - 모두 접근 가능
    private final String[] allowUris = {
            "/api/v1/auth/signup", // 회원가입
            "/api/v1/auth/login", //로그인
            "/api/v1/auth/refresh", // 재발급
            "/api/v1/auth/email/send-verification", // 이메일 전송
            "/api/v1/auth/email/verify", // 이메일 인증
            "/api/v1/auth/oauth2/token", // 소셜 로그인 후 토큰 발급
            "/api/v1/auth/social/**", // 소셜 로그인 요청
            "/login/**", // 소셜 로그인 이후 돌아옴
            "/actuator/health/**", // 헬스체크
            "/api/v1/auth/recover", // 계정복구 요청
    };

    //로그인 필요X - GET 요청만 가능
    private final String[] allowGetUris = {
            "/api/v1/policies/**"
    };

    // 관리자만 접근 가능
    private final String[] adminUris = {
            "/api/v1/admin/**",
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
                .authorizeHttpRequests(auth -> {
                    // Swagger 및 공용 경로
                    auth.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**").permitAll();
                    auth.requestMatchers(allowUris).permitAll();

                    // GET 요청에 대해서만 모든 사용자에게 허용
                    auth.requestMatchers(HttpMethod.GET, allowGetUris).permitAll();

                    // 관리자 전용 경로
                    if (adminUris.length > 0) {
                        auth.requestMatchers(adminUris).hasAuthority(Role.ROLE_ADMIN.name());
                    }

                    // 나머지 모든 요청
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint))

                //
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

        // 프론트엔드 주소 허용 (localhost:3000 등)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",       // 로컬 테스트용
                "https://moamoa.io.kr",
                "https://www.moamoa.io.kr",
                "https://moamoamoa.netlify.app",
                "https://api.moamoa.io.kr",
                "http://localhost:5173",
                "https://localhost:5173"
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