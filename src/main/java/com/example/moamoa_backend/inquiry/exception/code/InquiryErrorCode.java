package com.example.moamoa_backend.inquiry.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum InquiryErrorCode implements BaseErrorCode {

    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND,"INQUIRY_404_01", "존재하지 않는 문의입니다."),
    INQUIRY_ALREADY_ANSWERED(HttpStatus.CONFLICT,"INQUIRY_409_01", "이미 답변이 등록된 문의입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
