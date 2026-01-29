package com.example.moamoa_backend.member.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {

    // 400 Bad Request
    /**
     * scope 파라미터가 허용된 값(ALL/INTERESTS/GOAL)이 아닐 때 사용하는 에러 코드.
     * - 일반적으로는 컨트롤러의 Enum 바인딩 단계에서 400이 발생하지만,
     *   팀 정책상 명시적으로 동일한 에러코드로 내려주고 싶을 때 사용 가능
     */
    INVALID_SCOPE(HttpStatus.BAD_REQUEST,
        "MEMBER400_1",
        "scope 값이 올바르지 않습니다."),

    PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST,
            "MEMBER400_2",
            "현재 비밀번호가 일치하지 않습니다."),

    PASSWORD_CONFIRM_NOT_MATCH(HttpStatus.BAD_REQUEST,
            "MEMBER400_3",
            "새 비밀번호와 확인이 일치하지 않습니다."),

    SAME_AS_OLD_PASSWORD(HttpStatus.BAD_REQUEST,
            "MEMBER400_4",
            "새 비밀번호는 기존 비밀번호와 달라야 합니다."),

    INVALID_GENDER(HttpStatus.BAD_REQUEST,
            "MEMBER400_5",
            "성별 값이 올바르지 않습니다."),

    SOCIAL_LOGIN_MEMBER(HttpStatus.BAD_REQUEST,
            "MEMBER400_6",
            "소셜 로그인 회원은 비밀번호를 변경할 수 없습니다."),

    // 403 Forbidden
    MEMBER_BANNED(HttpStatus.FORBIDDEN,
            "MEMBER403_1",
            "차단된 회원입니다."),

    MEMBER_WITHDRAWN(HttpStatus.FORBIDDEN,
            "MEMBER403_2",
            "탈퇴한 회원입니다."),

    // 404 Not Found
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND,
            "MEMBER404_1",
            "존재하지 않는 회원입니다."),

    // 409 Conflict
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT,
            "MEMBER409_1",
            "이미 사용 중인 이메일입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

}
