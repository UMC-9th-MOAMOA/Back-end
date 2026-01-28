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

    MEMBER_GET_PROFILE(HttpStatus.OK,
            "MEMBER200_1",
            "회원 정보 조회에 성공했습니다."),

    MEMBER_UPDATE(HttpStatus.OK,
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

    MEMBER_GET_DAILY_GOAL_RESULT(HttpStatus.OK,
        "MEMBER200_7",
        "일간 목표 결과 조회에 성공했습니다."),

    MEMBER_GET_WEEKLY_GOAL_RESULT(HttpStatus.OK,
        "MEMBER200_8",
        "주간 목표 결과 조회에 성공했습니다."),

    PASSWORD_CHANGED(HttpStatus.OK,
            "MEMBER200_9",
            "비밀번호가 성공적으로 변경되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
