package com.example.moamoa_backend.inquiry.exception;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import com.example.moamoa_backend.global.apiPayload.exception.GeneralException;

public class InquiryException extends GeneralException {
    public InquiryException(BaseErrorCode code) {
        super(code);
    }
}
