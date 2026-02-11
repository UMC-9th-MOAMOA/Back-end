package com.example.moamoa_backend.global.apiPayload.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum GeneralSuccessCode implements BaseSuccessCode {

	//200
	OK(HttpStatus.OK,
		"COMMON200_1",
		"요청이 성공적으로 처리되었습니다."),
	//201
	CREATED(HttpStatus.CREATED,
		"COMMON201_1",
		"리소스가 성공적으로 생성되었습니다."),
	//202
	ACCEPTED(HttpStatus.ACCEPTED,
		"COMMON202_1",
		"요청이 접수되었습니다."),
	//204
	NO_CONTENT(HttpStatus.NO_CONTENT,
		"COMMON204_1",
		"요청이 성공적으로 처리되었으나 반환할 내용이 없습니다."),
	;

	private final HttpStatus status;
	private final String code;
	private final String message;
}
