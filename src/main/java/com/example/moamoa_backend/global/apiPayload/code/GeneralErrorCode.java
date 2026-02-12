package com.example.moamoa_backend.global.apiPayload.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum GeneralErrorCode implements BaseErrorCode {

	// ============= 400 Bad Request =============
	// ---- Common ----
	BAD_REQUEST(HttpStatus.BAD_REQUEST,
		"COMMON400_1",
		"잘못된 요청입니다."),

	MESSAGE_NOT_READABLE(HttpStatus.BAD_REQUEST,
		"COMMON400_2",
		"요청 본문을 읽을 수 없습니다."),

	// ---- Validation ----
	VALIDATION_ERROR(HttpStatus.BAD_REQUEST,
		"VALIDATION400_1",
		"입력값 검증에 실패했습니다."),

	METHOD_ARGUMENT_NOT_VALID(HttpStatus.BAD_REQUEST,
		"VALIDATION400_2",
		"요청 바디 검증에 실패했습니다."),

	BINDING_ERROR(HttpStatus.BAD_REQUEST,
		"VALIDATION400_3",
		"요청값 바인딩에 실패했습니다."),

	CONSTRAINT_VIOLATION(HttpStatus.BAD_REQUEST,
		"VALIDATION400_4",
		"요청 값 검증에 실패했습니다."),

	// ============= 404 Not Found =============
	NOT_FOUND(HttpStatus.NOT_FOUND,
		"COMMON404_1",
		"요청한 리소스를 찾을 수 없습니다."),

	// ============= 500 Internal Server Error =============
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,
		"COMMON500_1",
		"예기치 않은 서버 에러가 발생했습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
