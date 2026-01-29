package com.example.moamoa_backend.auth.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    // ============= 400 Bad Request =============
    VERIFICATION_CODE_INVALID(HttpStatus.BAD_REQUEST,
        "AUTH400_1",
        "이메일 인증 번호가 일치하지 않습니다."),

    // 이메일 검증 성공 이후 회원가입까지 오래걸릴 경우
    VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST,
        "AUTH400_4",
        "인증 시간이 만료되었습니다. 다시 발송해주세요."),

    // 이메일 인증번호 검증 최대 횟수 제한
    VERIFICATION_ATTEMPTS_EXCEEDED(HttpStatus.BAD_REQUEST,
        "AUTH400_5",
        "인증 시도 횟수를 초과했습니다. 다시 발송해주세요."),

    // 하나의 이메일에 검증메일 cool-down
    EMAIL_SEND_BLOCKED(HttpStatus.BAD_REQUEST,
        "AUTH400_6",
        "이메일 재전송은 30초 뒤에 가능합니다."),

    REFRESH_TOKEN_MISSING(HttpStatus.BAD_REQUEST,
        "AUTH400_7",
        "Refresh Token 쿠키가 요청에 포함되지 않았습니다."),

    UNSUPPORTED_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST,
        "AUTH400_8",
        "지원하지 않는 소셜 로그인 플랫폼입니다."),

    INVALID_RECOVER_REQUEST(HttpStatus.BAD_REQUEST,
            "AUTH400_9",
            "복구할 수 없는 계정입니다."),

    // ============= 401 Unauthorized =============
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED,
        "AUTH401_1",
        "이메일 또는 비밀번호가 일치하지 않습니다."),

    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED,
        "AUTH401_2",
        "로그인이 필요합니다."),

    // 소셜 로그인 redirect 코드로 최초 토큰발급 상황
    INVALID_OAUTH_CODE(HttpStatus.UNAUTHORIZED,
        "AUTH401_3",
        "유효하지 않거나 만료된 소셜 로그인 코드입니다."),

    // ============= 403 Forbidden =============
    ACCESS_DENIED(HttpStatus.FORBIDDEN,
        "AUTH403_1",
        "접근 권한이 없습니다."),

    ACCOUNT_WITHDRAWN(HttpStatus.FORBIDDEN,
            "AUTH403_2",
            "탈퇴한 계정입니다. 계정 복구를 원하시면 복구 요청을 진행해주세요."),

    ACCOUNT_BANNED(HttpStatus.FORBIDDEN,
            "AUTH403_3",
            "정지된 계정입니다. 문의사항은 관리자(moamoamoa2026@gmail.com)에게 연락해주세요."),

    MEMBER_NOT_ACTIVE(HttpStatus.FORBIDDEN,
            "AUTH403_4",
            "활성화되지 않은 계정입니다. 고객센터에 문의해주세요."),

    // AuthErrorCode 또는 MemberErrorCode에 추가
    POLICY_NOT_AGREED(HttpStatus.FORBIDDEN,
            "AUTH403_5",
            "아직 정책에 동의하지 않은 회원입니다. 정책 동의 후 이용해주세요."),

    // ============= 429 Too Many Requests =============
    IP_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS,
        "AUTH429_1",
        "비정상적인 요청 감지로 1시간 동안 인증이 제한됩니다."),

    // ============= 500 Internal Server Error =============
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,
        "AUTH500_1",
        "이메일 전송 중 오류가 발생했습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}