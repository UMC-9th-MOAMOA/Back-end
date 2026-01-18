package com.example.moamoa_backend.global.security.jwt.exception;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import com.example.moamoa_backend.global.apiPayload.exception.GeneralException;

public class JwtException extends GeneralException {
    public JwtException(BaseErrorCode code) {
        super(code);
    }
}
