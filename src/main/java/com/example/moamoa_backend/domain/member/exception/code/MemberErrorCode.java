package com.example.moamoa_backend.domain.member.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {

	// ============= 400 Bad Request =============
	/**
	 * scope 파라미터가 허용된 값(ALL/INTERESTS/GOAL)이 아닐 때 사용하는 에러 코드.
	 * - 일반적으로는 컨트롤러의 Enum 바인딩 단계에서 400이 발생하지만,
	 *   팀 정책상 명시적으로 동일한 에러코드로 내려주고 싶을 때 사용 가능
	 */
	INVALID_SCOPE(HttpStatus.BAD_REQUEST,
		"MEMBER400_1",
		"scope 값이 올바르지 않습니다."),

	INVALID_GENDER(HttpStatus.BAD_REQUEST,
		"MEMBER400_5",
		"성별 값이 올바르지 않습니다."),

	// ---- Onboarding (400) ----
	ONBOARDING_SELECTION_REQUIRED(
		HttpStatus.BAD_REQUEST,
		"ONBOARDING400_1",
		"관심사는 최소 1개 이상 선택해야 합니다."
	),

	ONBOARDING_GOAL_OUT_OF_RANGE(
		HttpStatus.BAD_REQUEST,
		"ONBOARDING400_4",
		"일일 목표는 1~5 사이여야 합니다."
	),

	ONBOARDING_GOAL_RETENTION_INVALID(
		HttpStatus.BAD_REQUEST,
		"ONBOARDING400_7",
		"목표 유지 기간 입력이 올바르지 않습니다."
	),

	// ============= 403 Forbidden =============
	MEMBER_BANNED(HttpStatus.FORBIDDEN,
		"MEMBER403_1",
		"차단된 회원입니다."),

	MEMBER_WITHDRAWN(HttpStatus.FORBIDDEN,
		"MEMBER403_2",
		"탈퇴한 회원입니다."),

	// ============= 404 Not Found =============
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND,
		"MEMBER404_1",
		"존재하지 않는 회원입니다."),

	// ---- Goal (404) ----
	GOAL_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND,
		"GOAL_RESULT_404",
		"목표 결과가 존재하지 않습니다."),

	// ============= 409 Conflict =============
	EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT,
		"MEMBER409_1",
		"이미 사용 중인 이메일입니다."),

	// ============= 500 Internal Server Error =============
	GOAL_APPLY_DATE_REQUIRED(HttpStatus.INTERNAL_SERVER_ERROR,
		"MEMBER500_1",
		"목표 유지 기간 설정 시 적용 시작일(startDate)이 누락되었습니다."),

	GOAL_APPLY_DATE_MUST_BE_MONDAY(HttpStatus.INTERNAL_SERVER_ERROR,
		"MEMBER500_2",
		"목표 적용 시작일(startDate)은 월요일이어야 합니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
