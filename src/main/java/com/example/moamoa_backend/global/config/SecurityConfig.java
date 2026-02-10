package com.example.moamoa_backend.global.config;

import com.example.moamoa_backend.domain.auth.handler.OAuth2SuccessHandler;
import com.example.moamoa_backend.domain.auth.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import com.example.moamoa_backend.domain.auth.service.CustomOAuth2UserService;
import com.example.moamoa_backend.global.security.filter.MemberSetupFilter;
import com.example.moamoa_backend.global.security.jwt.JwtAccessDeniedHandler;
import com.example.moamoa_backend.global.security.jwt.JwtAuthFilter;
import com.example.moamoa_backend.global.security.jwt.JwtAuthenticationEntryPoint;
import com.example.moamoa_backend.domain.member.enums.Role;
import com.example.moamoa_backend.domain.member.repository.MemberRepository;

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
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 설정
 *
 * 필터 체인 순서:
 * JwtAuthFilter → ExceptionTranslationFilter → MemberSetupFilter → AuthorizationFilter
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
	private final MemberRepository memberRepository;

	// 로그인 필요X - 모두 접근 가능
	private final String[] allowUris = {
		// 가입, 로그인, 탈퇴복구, Refresh
		"/api/v1/auth/signup",
		"/api/v1/auth/login",
		"/api/v1/auth/recover",
		"/api/v1/auth/refresh",

		// 비밀번호 초기화
		"/api/v1/auth/password-resets", // POST, PUT
		"/api/v1/auth/password-resets/verifications",

		// 회원가입 이메일 인증
		"/api/v1/auth/email/verification-codes",
		"/api/v1/auth/email/verifications",

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

    // 로그인 필요O - 초기 설정(정책동의, 온보딩) 완료 전에도 접근 가능
    private final List<RequestMatcher> setupAllowMatchers = List.of(
            PathPatternRequestMatcher.withDefaults().matcher("/api/v1/auth/**"),
            PathPatternRequestMatcher.withDefaults().matcher("/api/v1/policies/**"),
            PathPatternRequestMatcher.withDefaults().matcher("/api/v1/members/me/onboarding"),
            PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.DELETE, "/api/v1/members/me"),
            PathPatternRequestMatcher.withDefaults().matcher("/api/v1/interests/**"),
            PathPatternRequestMatcher.withDefaults().matcher("/v3/api-docs/**"),
            PathPatternRequestMatcher.withDefaults().matcher("/swagger-ui/**"),
            PathPatternRequestMatcher.withDefaults().matcher("/swagger-resources/**")
    );

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

            // 필터 순서: JwtAuthFilter → (ExceptionTranslationFilter) → MemberSetupFilter
			.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterAfter(memberSetupFilter(), ExceptionTranslationFilter.class)

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

		configuration.setAllowedOriginPatterns(Arrays.asList(
			"http://localhost:3000",
			"https://moamoa.io.kr",
			"https://www.moamoa.io.kr",
			"https://moamoamoa.netlify.app",
			"https://api.moamoa.io.kr",
			"http://localhost:5173",
			"https://localhost:5173",
                "https://*--moamoamoa.netlify.app"
		));

		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public MemberSetupFilter memberSetupFilter() {
		return new MemberSetupFilter(setupAllowMatchers, memberRepository);
    }

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}