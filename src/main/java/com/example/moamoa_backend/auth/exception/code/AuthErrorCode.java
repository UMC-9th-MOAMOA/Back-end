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
        "이메일 인증 번호가 일치하지 않습니다."),

    REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST,
        "AUTH400_2",
        "필수 약관에 모두 동의해야 합니다."),

    INVALID_POLICY_ID(HttpStatus.BAD_REQUEST,
        "AUTH400_3",
        "존재하지 않는 약관이 포함되어 있습니다."),

    VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST,
        "AUTH400_4",
        "인증 시간이 만료되었습니다. 다시 발송해주세요."),

    VERIFICATION_ATTEMPTS_EXCEEDED(HttpStatus.BAD_REQUEST,
        "AUTH400_5",
        "인증 시도 횟수를 초과했습니다. 다시 발송해주세요."),

    EMAIL_SEND_BLOCKED(HttpStatus.BAD_REQUEST,
        "AUTH400_6",
        "이메일 재전송은 30초 뒤에 가능합니다."),

    REFRESH_TOKEN_MISSING(HttpStatus.BAD_REQUEST,
        "AUTH400_7",
        "Refresh Token 쿠키가 요청에 포함되지 않았습니다."),

    UNSUPPORTED_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST,
        "AUTH400_8",
        "지원하지 않는 소셜 로그인 플랫폼입니다."),

    EMAIL_NOT_PROVIDED(HttpStatus.BAD_REQUEST,
        "AUTH400_9",
        "소셜 로그인 시 이메일 제공에 동의해야 서비스를 이용할 수 있습니다."),

    LOGIN_FAILED(HttpStatus.UNAUTHORIZED,
        "AUTH401_1",
        "이메일 또는 비밀번호가 일치하지 않습니다."),

    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED,
        "AUTH401_2",
        "로그인이 필요합니다."),

    INVALID_OAUTH_CODE(HttpStatus.UNAUTHORIZED,
        "AUTH401_3",
        "유효하지 않거나 만료된 소셜 로그인 코드입니다."),

    ACCESS_DENIED(HttpStatus.FORBIDDEN,
        "AUTH403_1",
        "접근 권한이 없습니다."),

    IP_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS,
        "AUTH429_1",
        "비정상적인 요청 감지로 1시간 동안 인증이 제한됩니다."),

    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,
        "AUTH500_1",
        "이메일 전송 중 오류가 발생했습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}