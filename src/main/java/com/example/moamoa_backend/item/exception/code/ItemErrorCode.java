package com.example.moamoa_backend.item.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ItemErrorCode implements BaseErrorCode {

	ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "ITEM404_1", "아이템을 찾을 수 없습니다."),
	ITEM_NOT_ON_SALE(HttpStatus.BAD_REQUEST, "ITEM400_1", "판매 중인 아이템이 아닙니다."),
	ITEM_ALREADY_OWNED(HttpStatus.CONFLICT, "ITEM409_1", "이미 보유한 아이템입니다."),
	ITEM_NOT_OWNED(HttpStatus.BAD_REQUEST, "ITEM400_2", "보유하지 않은 아이템입니다."),

	MEMBER_ITEM_MEMBER_NULL(HttpStatus.INTERNAL_SERVER_ERROR, "ITEM500_1", "서버 오류: member가 null입니다."),
	MEMBER_ITEM_ITEM_NULL(HttpStatus.INTERNAL_SERVER_ERROR, "ITEM500_2", "서버 오류: item이 null입니다.");


	private final HttpStatus status;
	private final String code;
	private final String message;
}
