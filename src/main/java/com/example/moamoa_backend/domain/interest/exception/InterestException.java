package com.example.moamoa_backend.domain.interest.exception;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import com.example.moamoa_backend.global.apiPayload.exception.GeneralException;

public class InterestException extends GeneralException {
	public InterestException(BaseErrorCode code) {
		super(code);
	}
}
