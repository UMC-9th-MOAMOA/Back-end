package com.example.moamoa_backend.inquiry.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum InquirySuccessCode implements BaseSuccessCode {
    INQUIRY_GET_PROFILE(HttpStatus.OK,
            "INQUIRY200_2",
            "문의 조회에 성공했습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}
