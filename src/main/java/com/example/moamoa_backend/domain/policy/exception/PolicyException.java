package com.example.moamoa_backend.domain.policy.exception;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import com.example.moamoa_backend.global.apiPayload.exception.GeneralException;

public class PolicyException extends GeneralException {
    public PolicyException(BaseErrorCode code) {
        super(code);
    }
}
