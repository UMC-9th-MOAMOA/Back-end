package com.example.moamoa_backend.interest.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum InterestErrorCode implements BaseErrorCode {

	INTEREST_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"INTEREST404_1",
		"해당 관심사를 찾을 수 없습니다."
	),
	SUB_INTEREST_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"INTEREST404_2",
		"해당 세부 관심사를 찾을 수 없습니다."
	),
	SUB_INTEREST_MISMATCH_INTEREST(
		HttpStatus.BAD_REQUEST,
		"INTEREST400_1",
		"세부 관심사가 대분류 관심사에 속하지 않습니다."
	),

	ONBOARDING_SELECTION_REQUIRED(
		HttpStatus.BAD_REQUEST,
		"ONBOARDING400_1",
		"관심사는 최소 1개 이상 선택해야 합니다."
	),
	ONBOARDING_GOAL_REQUIRED(
		HttpStatus.BAD_REQUEST,
		"ONBOARDING400_3",
		"일일 목표는 필수 입력입니다."
	),
	ONBOARDING_GOAL_OUT_OF_RANGE(
		HttpStatus.BAD_REQUEST,
		"ONBOARDING400_4",
		"일일 목표는 0~5 사이여야 합니다."
	),
	INVALID_SCOPE(
		HttpStatus.BAD_REQUEST,
		"ONBOARDING400_5",
		"scope 값이 올바르지 않습니다."
	);

	private final HttpStatus status;
	private final String code;
	private final String message;
}
