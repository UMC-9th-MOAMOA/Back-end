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
	);

	private final HttpStatus status;
	private final String code;
	private final String message;
}
