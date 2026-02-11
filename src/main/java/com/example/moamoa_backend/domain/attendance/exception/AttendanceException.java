package com.example.moamoa_backend.domain.attendance.exception;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import com.example.moamoa_backend.global.apiPayload.exception.GeneralException;

public class AttendanceException extends GeneralException {
	public AttendanceException(BaseErrorCode code) {
		super(code);
	}
}
