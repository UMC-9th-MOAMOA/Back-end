package com.example.moamoa_backend.global.apiPayload.handler;

import com.example.moamoa_backend.auth.exception.code.AuthErrorCode;
import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import com.example.moamoa_backend.global.apiPayload.code.GeneralErrorCode;
import com.example.moamoa_backend.global.apiPayload.exception.GeneralException;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GeneralExceptionAdvice {

    // GeneralException 처리
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(GeneralException e) {

        return ResponseEntity.status(e.getCode().getStatus())
                .body(ApiResponse.onFailure(
                                e.getCode(),
                                null
                        )
                );
    }

    /**
     * @CookieValue(required=true) 검증 실패 시 (쿠키 누락)
     */
    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingRequestCookieException(MissingRequestCookieException e) {

        log.warn("필수 쿠키 누락: {}", e.getCookieName());

        return ResponseEntity
                .status(AuthErrorCode.REFRESH_TOKEN_MISSING.getStatus())
                .body(ApiResponse.onFailure(AuthErrorCode.REFRESH_TOKEN_MISSING, null));
    }

    //예상하지 못한 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        BaseErrorCode errorCode = GeneralErrorCode.INTERNAL_SERVER_ERROR;

        log.error("예상치 못한 에러 발생: ", e);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.onFailure(errorCode, null));
    }
}

