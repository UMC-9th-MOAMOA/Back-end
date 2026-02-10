package com.example.moamoa_backend.inquiry.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum InquirySuccessCode implements BaseSuccessCode {
    // 201
    INQUIRY_CREATE_SUCCESS(
            HttpStatus.CREATED,
            "INQUIRY201_1",
                    "문의 등록에 성공했습니다."
    ),
    INQUIRY_ANSWER_CREATE_SUCCESS(
            HttpStatus.CREATED,
            "INQUIRY201_2",
                    "문의 답변 등록에 성공했습니다."
    ),

    // 200
    INQUIRY_LIST_SUCCESS(
            HttpStatus.OK,
            "INQUIRY200_1",
                    "문의 목록 조회에 성공했습니다."
    ),
    INQUIRY_DETAIL_SUCCESS(
            HttpStatus.OK,
            "INQUIRY200_2",
                    "문의 상세 조회에 성공했습니다."
    );


    private final HttpStatus status;
    private final String code;
    private final String message;
}
