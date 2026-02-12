package com.example.moamoa_backend.domain.inquiry.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum InquiryErrorCode implements BaseErrorCode {

	// ============= 400 Bad Request =============
	TOO_MANY_IMAGES(
		HttpStatus.BAD_REQUEST,
		"INQUIRY400_1",
		"이미지는 최대 5개까지 업로드할 수 있습니다."),

	TOO_MANY_ANSWER_IMAGES(
		HttpStatus.BAD_REQUEST,
		"INQUIRY400_2",
		"답변 이미지는 최대 5개까지 업로드할 수 있습니다."),

	// ============= 403 Forbidden =============
	TERMS_NOT_AGREED(
		HttpStatus.FORBIDDEN,
		"INQUIRY403_1",
		"약관 동의 후 문의 등록이 가능합니다."),

	// ============= 404 Not Found =============
	INQUIRY_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"INQUIRY404_1",
		"존재하지 않는 문의입니다."),

	// ============= 409 Conflict =============
	INQUIRY_ALREADY_ANSWERED(
		HttpStatus.CONFLICT,
		"INQUIRY409_1",
		"이미 답변이 등록된 문의입니다."),

	// ============= 500 Internal Server Error =============
	IMAGE_UPLOAD_FAILED(
		HttpStatus.INTERNAL_SERVER_ERROR,
		"INQUIRY500_1",
		"이미지 업로드 중 오류가 발생했습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
