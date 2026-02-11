package com.example.moamoa_backend.domain.auth.exception;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import com.example.moamoa_backend.global.apiPayload.exception.GeneralException;

public class AuthException extends GeneralException {
	public AuthException(BaseErrorCode code) {
		super(code);
	}
}