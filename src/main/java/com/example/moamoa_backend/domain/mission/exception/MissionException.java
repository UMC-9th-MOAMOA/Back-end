package com.example.moamoa_backend.domain.mission.exception;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import com.example.moamoa_backend.global.apiPayload.exception.GeneralException;

public class MissionException extends GeneralException {
	public MissionException(BaseErrorCode code) {
		super(code);
	}
}
