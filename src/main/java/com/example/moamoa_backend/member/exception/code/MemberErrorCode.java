package com.example.moamoa_backend.member.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND,
            "MEMBER404_1",
            "존재하지 않는 회원입니다."),

    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT,
            "MEMBER409_1",
            "이미 사용 중인 이메일입니다."),

    MEMBER_BANNED(HttpStatus.FORBIDDEN,
            "MEMBER403_1",
            "차단된 회원입니다."),

    MEMBER_WITHDRAWN(HttpStatus.FORBIDDEN,
            "MEMBER403_2",
            "탈퇴한 회원입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

}
