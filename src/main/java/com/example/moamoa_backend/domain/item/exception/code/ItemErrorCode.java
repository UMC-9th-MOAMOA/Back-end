package com.example.moamoa_backend.domain.item.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ItemErrorCode implements BaseErrorCode {

	//404
	ITEM_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"ITEM404_1",
		"아이템을 찾을 수 없습니다."),

	//409
	ITEM_ALREADY_OWNED(
		HttpStatus.CONFLICT,
		"ITEM409_1",
		"이미 보유한 아이템입니다."),

	//400
	ITEM_NOT_ON_SALE(
		HttpStatus.BAD_REQUEST,
		"ITEM400_1",
		"판매 중인 아이템이 아닙니다."),
	ITEM_NOT_OWNED(
		HttpStatus.BAD_REQUEST,
		"ITEM400_2",
		"보유하지 않은 아이템입니다."),
	ITEM_INVALID_CATEGORY_TYPE(
		HttpStatus.BAD_REQUEST,
		"ITEM400_3",
		"요청한 카테고리와 아이템 타입이 일치하지 않습니다."),
	ITEM_INVALID_CATEGORY(
		HttpStatus.BAD_REQUEST,
		"ITEM400_4",
		"유효하지 않은 아이템 카테고리 요청입니다."),
	ITEM_INVALID_TYPE(
		HttpStatus.BAD_REQUEST,
		"ITEM400_5",
		"유효하지 않은 아이템 타입 요청입니다."),


	//500
	MEMBER_ITEM_MEMBER_NULL(
		HttpStatus.INTERNAL_SERVER_ERROR,
		"ITEM500_1",
		"서버 오류: member가 null입니다."),
	MEMBER_ITEM_ITEM_NULL(
		HttpStatus.INTERNAL_SERVER_ERROR,
		"ITEM500_2",
		"서버 오류: item이 null입니다.");


	private final HttpStatus status;
	private final String code;
	private final String message;
}
