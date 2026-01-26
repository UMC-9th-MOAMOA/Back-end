package com.example.moamoa_backend.policy.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PolicySuccessCode implements BaseSuccessCode {

    POLICY_GET_SUCCESS(HttpStatus.OK,
            "POLICY200_1",
            "약관 상세 조회를 완료했습니다."),

    POLICY_LIST_GET_SUCCESS(HttpStatus.OK,
            "POLICY200_2",
            "약관 목록 조회를 완료했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}