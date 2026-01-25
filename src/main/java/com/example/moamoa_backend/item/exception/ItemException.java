package com.example.moamoa_backend.item.exception;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import com.example.moamoa_backend.global.apiPayload.exception.GeneralException;

public class ItemException extends GeneralException {
	public ItemException(BaseErrorCode code) {
		super(code);
	}
}
