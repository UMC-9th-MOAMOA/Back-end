package com.example.moamoa_backend.auth.exception;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import com.example.moamoa_backend.global.apiPayload.exception.GeneralException;


public class AuthException extends GeneralException {
    /**
     * Create a new AuthException for the specified authentication error.
     *
     * @param code the BaseErrorCode representing the specific authentication error
     */
    public AuthException(BaseErrorCode code) {
        super(code);
    }
}