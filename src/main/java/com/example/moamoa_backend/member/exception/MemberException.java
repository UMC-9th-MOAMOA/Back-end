package com.example.moamoa_backend.member.exception;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import com.example.moamoa_backend.global.apiPayload.exception.GeneralException;

public class MemberException extends GeneralException {
    /**
     * Create a MemberException for the specified member-related error code.
     *
     * @param code the BaseErrorCode that identifies the specific member error
     */
    public MemberException(BaseErrorCode code) {
        super(code);
    }
}
