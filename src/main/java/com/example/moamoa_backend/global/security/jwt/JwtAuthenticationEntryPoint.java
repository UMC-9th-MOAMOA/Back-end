package com.example.moamoa_backend.global.security.jwt;

import com.example.moamoa_backend.domain.auth.exception.code.AuthErrorCode;
import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import com.example.moamoa_backend.global.apiPayload.code.GeneralErrorCode;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final JsonMapper jsonMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        // 1. 필터에서 담아둔 에러 코드 꺼내기
        Object exceptionAttribute = request.getAttribute("exception");

        BaseErrorCode errorCode = AuthErrorCode.AUTHENTICATION_REQUIRED ;// 기본값

        if (exceptionAttribute instanceof BaseErrorCode) {
            errorCode = (BaseErrorCode) exceptionAttribute;
        }

        // 2. 로깅
        log.warn("Authentication failed: [{}] {} | URI: {}",
                errorCode.getCode(),
                errorCode.getMessage(),
                request.getRequestURI()
        );

        // 3. 응답 설정
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(errorCode.getStatus().value());

        // 4. JSON 응답 작성
        ApiResponse<Void> errorResponse = ApiResponse.onFailure(errorCode, null);
        jsonMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}