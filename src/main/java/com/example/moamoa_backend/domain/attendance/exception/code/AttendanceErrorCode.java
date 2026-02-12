package com.example.moamoa_backend.domain.attendance.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AttendanceErrorCode implements BaseErrorCode {

	// ============= 401 Unauthorized =============
	UNAUTHORIZED(
		HttpStatus.UNAUTHORIZED,
		"ATTENDANCE401_1",
		"인증 정보가 없습니다."),

	// ============= 404 Not Found =============
	MEMBER_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"ATTENDANCE404_1",
		"회원 정보를 찾을 수 없습니다."),

	// ============= 409 Conflict =============
	ALREADY_ATTENDED(
		HttpStatus.CONFLICT,
		"ATTENDANCE409_1",
		"오늘은 이미 출석했습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
