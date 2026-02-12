package com.example.moamoa_backend.domain.auth.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseSuccessCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthSuccessCode implements BaseSuccessCode {

	// ============= 200 OK =============
	LOGIN_SUCCESS(HttpStatus.OK,
		"AUTH200_1",
		"로그인에 성공했습니다."),

	LOGOUT_SUCCESS(HttpStatus.OK,
		"AUTH200_2",
		"로그아웃에 성공했습니다."),

	REISSUE_SUCCESS(HttpStatus.OK,
		"AUTH200_3",
		"토큰 재발급에 성공했습니다."),

	EMAIL_SEND_SUCCESS(HttpStatus.OK,
		"AUTH200_4",
		"인증 메일이 전송되었습니다."),

	EMAIL_VERIFY_SUCCESS(HttpStatus.OK,
		"AUTH200_5",
		"이메일 인증에 성공했습니다."),

	RECOVER_SUCCESS(HttpStatus.OK,
		"AUTH200_6",
		"계정이 복구되었습니다."),

	PASSWORD_RESET_SUCCESS(HttpStatus.OK,
		"AUTH200_7",
		"비밀번호가 성공적으로 변경되었습니다."),

	PASSWORD_CHANGED(HttpStatus.OK,
		"AUTH200_8",
		"비밀번호가 성공적으로 변경되었습니다."),

	// ============= 201 Created =============
	SIGNUP_SUCCESS(HttpStatus.CREATED,
		"AUTH201_1",
		"회원가입이 완료되었습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}