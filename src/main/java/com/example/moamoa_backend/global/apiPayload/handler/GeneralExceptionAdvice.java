package com.example.moamoa_backend.global.apiPayload.handler;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import com.example.moamoa_backend.global.apiPayload.code.GeneralErrorCode;
import com.example.moamoa_backend.global.apiPayload.exception.GeneralException;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.member.exception.code.MemberErrorCode;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GeneralExceptionAdvice {

    /**
     *   GeneralException 처리
     */
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
     * @RequestParam, @PathVariable 등에서 타입 변환 실패할 때 발생
     * 예) ?scope=AAA (OnboardingUpdateScope로 변환 실패)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {

        // scope 파라미터에 대해서만 명시적으로 INVALID_SCOPE로 내려줌
        if ("scope".equals(e.getName()) && e.getRequiredType() != null && e.getRequiredType().isEnum()) {
            return ResponseEntity
                .status(MemberErrorCode.INVALID_SCOPE.getStatus())
                .body(ApiResponse.onFailure(MemberErrorCode.INVALID_SCOPE, null));
        }

        // scope 외 다른 파라미터 타입 미스매치는 일단 500이 아니라 400으로 내림
        BaseErrorCode errorCode = GeneralErrorCode.BAD_REQUEST;
        log.warn("Type mismatch: param={}, value={}, requiredType={}",
            e.getName(), e.getValue(), e.getRequiredType());

        return ResponseEntity
            .status(errorCode.getStatus())
            .body(ApiResponse.onFailure(errorCode, null));
    }

    /**
     *   예상하지 못한 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        BaseErrorCode errorCode = GeneralErrorCode.INTERNAL_SERVER_ERROR;

        log.error("예상치 못한 에러 발생: ", e);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.onFailure(errorCode, null));
    }
}

