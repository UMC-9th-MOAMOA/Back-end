package com.example.moamoa_backend.domain.item.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseSuccessCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ItemSuccessCode implements BaseSuccessCode {

	// ============= 200 OK =============
	// ---- Item ----
	ITEM_LIST_OK(
		HttpStatus.OK,
		"ITEM200_1",
		"아이템 목록 조회에 성공했습니다."),

	ITEM_PURCHASE_OK(
		HttpStatus.OK,
		"ITEM200_2",
		"아이템 구매에 성공했습니다."),

	AVATAR_EQUIP_OK(
		HttpStatus.OK,
		"ITEM200_3",
		"아바타 착장 변경에 성공했습니다."),

	// ---- Home ----
	HOME_OK(
		HttpStatus.OK,
		"HOME200_1",
		"홈 조회에 성공했습니다."),

	HOME_POCKET_OK(
		HttpStatus.OK,
		"HOME200_2",
		"홈 주머니 조회에 성공했습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
