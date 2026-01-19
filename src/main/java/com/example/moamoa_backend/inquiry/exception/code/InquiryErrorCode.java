package com.example.moamoa_backend.inquiry.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum InquiryErrorCode implements BaseErrorCode {

    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND,"INQUIRY_404_01", "존재하지 않는 문의입니다."),
    INQUIRY_ALREADY_ANSWERED(HttpStatus.CONFLICT,"INQUIRY_409_01", "이미 답변이 등록된 문의입니다."),
    TOO_MANY_IMAGES(HttpStatus.BAD_REQUEST, "INQUIRY400_1", "이미지는 최대 5개까지 업로드할 수 있습니다."),
    TOO_MANY_ANSWER_IMAGES(HttpStatus.BAD_REQUEST, "INQUIRY400_2", "답변 이미지는 최대 5개까지 업로드할 수 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
