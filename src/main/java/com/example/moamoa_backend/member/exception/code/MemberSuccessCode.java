package com.example.moamoa_backend.member.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
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

    MEMBER_EMAIL_CHECK(HttpStatus.OK,
            "MEMBER200_1",
            "사용 가능한 이메일입니다."),

    MEMBER_GET_PROFILE(HttpStatus.OK,
            "MEMBER200_2",
            "회원 정보 조회에 성공했습니다."),

    MEMBER_UPDATE(HttpStatus.OK,
            "MEMBER200_3",
            "회원 정보 수정이 완료되었습니다."),

    MEMBER_WITHDRAW(HttpStatus.OK,
            "MEMBER200_4",
            "회원 탈퇴가 완료되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
