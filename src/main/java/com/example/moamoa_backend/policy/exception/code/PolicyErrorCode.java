package com.example.moamoa_backend.policy.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PolicyErrorCode implements BaseErrorCode {

    POLICY_NOT_FOUND(HttpStatus.NOT_FOUND,
            "POLICY404_1",
                    "존재하지 않는 정책입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
