package com.example.moamoa_backend.global.security.jwt.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum JwtErrorCode implements BaseErrorCode {

    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED,
            "JWT400_1",
            "토큰이 만료되었습니다."),

    TOKEN_INVALID(HttpStatus.UNAUTHORIZED,
            "JWT400_2",
            "유효하지 않은 토큰입니다."),

    TOKEN_MALFORMED(HttpStatus.UNAUTHORIZED,
            "JWT400_3",
            "잘못된 토큰 형식입니다."),

    TOKEN_EMPTY(HttpStatus.UNAUTHORIZED,
            "JWT400_4",
            "토큰이 없습니다."),

    TOKEN_UNSUPPORTED(HttpStatus.UNAUTHORIZED,
            "JWT400_5",
            "지원하지 않는 토큰입니다."),

    TOKEN_INVALID_TYPE(HttpStatus.UNAUTHORIZED,
            "JWT400_6",
            "토큰 타입이 올바르지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
