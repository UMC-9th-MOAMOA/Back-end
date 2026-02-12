package com.example.moamoa_backend.domain.member.exception;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import com.example.moamoa_backend.global.apiPayload.exception.GeneralException;

public class MemberException extends GeneralException {
	public MemberException(BaseErrorCode code) {
		super(code);
	}
}

