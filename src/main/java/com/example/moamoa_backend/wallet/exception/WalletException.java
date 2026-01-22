package com.example.moamoa_backend.wallet.exception;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import com.example.moamoa_backend.global.apiPayload.exception.GeneralException;

public class WalletException extends GeneralException {
	public WalletException(BaseErrorCode code) {
		super(code);
	}
}
