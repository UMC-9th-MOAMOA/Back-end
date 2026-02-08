package com.example.moamoa_backend.member.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseSuccessCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberSuccessCode implements BaseSuccessCode {

	MEMBER_SIGNUP(HttpStatus.CREATED,
		"MEMBER201_1",
		"회원가입이 완료되었습니다."),

	PROFILE_FETCHED(HttpStatus.OK,
		"MEMBER200_1",
		"회원 정보 조회에 성공했습니다."),

	MEMBER_UPDATED(HttpStatus.OK,
		"MEMBER200_2",
		"회원 정보 수정이 완료되었습니다."),

	MEMBER_WITHDRAW(HttpStatus.OK,
		"MEMBER200_3",
		"회원 탈퇴가 완료되었습니다. 30일 이내 재로그인 시 복구 가능합니다."),

	MEMBER_GET_ONBOARDING(HttpStatus.OK,
		"MEMBER200_5",
		"온보딩 조회에 성공했습니다."),

	MEMBER_UPDATE_ONBOARDING(HttpStatus.OK,
		"MEMBER200_6",
		"온보딩 수정에 성공했습니다."),
	MEMBER_GET_GOAL_POPUPS(HttpStatus.OK,
		"MEMBER200_7",
		"목표 팝업 조회에 성공했습니다."),

	MEMBER_MARK_GOAL_POPUP_SHOWN(HttpStatus.OK,
		"MEMBER200_8",
		"목표 팝업 확인 처리에 성공했습니다."),

	SETTING_SAVED(HttpStatus.OK,
		"MEMBER200_10",
		"설정이 저장되었습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
