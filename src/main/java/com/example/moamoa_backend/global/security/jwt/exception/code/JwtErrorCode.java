package com.example.moamoa_backend.global.security.jwt.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum JwtErrorCode implements BaseErrorCode {

	// ============= 400 Bad Request =============
	REFRESH_TOKEN_MISSING(HttpStatus.BAD_REQUEST,
		"JWT400_1",
		"Refresh Token 쿠키가 요청에 포함되지 않았습니다."),

	// ============= 401 Unauthorized =============
	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED,
		"JWT401_1",
		"토큰이 만료되었습니다."),

	TOKEN_INVALID(HttpStatus.UNAUTHORIZED,
		"JWT401_2",
		"유효하지 않은 토큰입니다."),

	TOKEN_MALFORMED(HttpStatus.UNAUTHORIZED,
		"JWT401_3",
		"잘못된 토큰 형식입니다."),

	TOKEN_EMPTY(HttpStatus.UNAUTHORIZED,
		"JWT401_4",
		"토큰이 없습니다."),

	TOKEN_UNSUPPORTED(HttpStatus.UNAUTHORIZED,
		"JWT401_5",
		"지원하지 않는 토큰입니다."),

	TOKEN_INVALID_TYPE(HttpStatus.UNAUTHORIZED,
		"JWT401_6",
		"토큰 타입이 올바르지 않습니다."),

	// ============= 500 Internal Server Error =============
	SECRET_KEY_INVALID(HttpStatus.INTERNAL_SERVER_ERROR,
		"JWT500_1",
		"JWT 시크릿 키는 최소 32바이트(256비트) 이상이어야 합니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}