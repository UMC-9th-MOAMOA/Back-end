package com.example.moamoa_backend.domain.interest.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseSuccessCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum InterestSuccessCode implements BaseSuccessCode {

	INTEREST_LIST_OK(
		HttpStatus.OK,
		"INTEREST200_1",
		"관심사 목록 조회에 성공했습니다."
	),

	SUB_INTEREST_LIST_OK(
		HttpStatus.OK,
		"INTEREST200_2",
		"세부 관심사 목록 조회에 성공했습니다."
	);

	private final HttpStatus status;
	private final String code;
	private final String message;
}
