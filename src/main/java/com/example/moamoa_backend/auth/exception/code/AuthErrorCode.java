package com.example.moamoa_backend.auth.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    VERIFICATION_CODE_INVALID(HttpStatus.BAD_REQUEST,
            "AUTH400_1",
            "인증 번호가 일치하지 않습니다."),

    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED,
            "AUTH401_1",
            "인증 토큰이 만료되었습니다."),

    TOKEN_INVALID(HttpStatus.UNAUTHORIZED,
            "AUTH401_2",
            "유효하지 않은 토큰입니다."),

    LOGIN_FAILED(HttpStatus.UNAUTHORIZED,
            "AUTH401_3",
            "이메일 또는 비밀번호가 일치하지 않습니다."),

    ACCESS_DENIED(HttpStatus.FORBIDDEN,
            "AUTH403_1",
            "접근 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
