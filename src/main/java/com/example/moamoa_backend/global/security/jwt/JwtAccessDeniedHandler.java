package com.example.moamoa_backend.global.security.jwt;

import com.example.moamoa_backend.domain.auth.exception.code.AuthErrorCode;
import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

/**
 * 인가(Authorization) 실패 처리 핸들러
 *
 * 인증된 사용자가 권한이 없는 리소스에 접근할 때 403 응답을 반환한다.
 *
 * 처리 케이스:
 * - USER가 ADMIN 전용 API 호출 → ACCESS_DENIED
 * - 정책 미동의 사용자가 일반 API 호출 → POLICY_NOT_AGREED
 * - 온보딩 미완료 사용자가 일반 API 호출 → ONBOARDING_NOT_COMPLETED
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

	private final JsonMapper jsonMapper;

	@Override
	public void handle(
		HttpServletRequest request,
		HttpServletResponse response,
		AccessDeniedException accessDeniedException
	) throws IOException {

		// 필터에서 담아둔 에러 코드 꺼내기
		Object exceptionAttribute = request.getAttribute("exception");

		BaseErrorCode errorCode = AuthErrorCode.ACCESS_DENIED; // 기본값

		if (exceptionAttribute instanceof BaseErrorCode) {
			errorCode = (BaseErrorCode)exceptionAttribute;
		}

		// 로깅
		log.warn("Access denied: [{}] {} | URI: {}",
			errorCode.getCode(),
			errorCode.getMessage(),
			request.getRequestURI()
		);

		// 응답 설정
		response.setContentType("application/json;charset=UTF-8");
		response.setStatus(errorCode.getStatus().value());

		// JSON 응답 작성
		ApiResponse<Void> errorResponse = ApiResponse.onFailure(errorCode, null);
		jsonMapper.writeValue(response.getOutputStream(), errorResponse);
	}
}