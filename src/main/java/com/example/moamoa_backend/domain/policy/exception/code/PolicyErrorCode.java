package com.example.moamoa_backend.domain.policy.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PolicyErrorCode implements BaseErrorCode {

    MANDATORY_AGREEMENT_REQUIRED(HttpStatus.BAD_REQUEST,
            "POLICY400_1",
            "필수 약관에 모두 동의해야 합니다."),

    DUPLICATE_POLICY_ID(HttpStatus.BAD_REQUEST,
            "POLICY400_2",
            "중복된 약관 ID가 존재합니다."),

    POLICY_NOT_FOUND(HttpStatus.NOT_FOUND,
            "POLICY404_1",
                    "존재하지 않는 정책입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
