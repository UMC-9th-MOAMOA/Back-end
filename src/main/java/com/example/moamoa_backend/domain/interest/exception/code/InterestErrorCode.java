package com.example.moamoa_backend.domain.interest.exception.code;

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
	);

	private final HttpStatus status;
	private final String code;
	private final String message;
}