package com.example.moamoa_backend.domain.wallet.exception;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import com.example.moamoa_backend.global.apiPayload.exception.GeneralException;

import lombok.Getter;

@Getter
public class WalletException extends GeneralException {
	private final Integer shortfall; //  추가

	public WalletException(BaseErrorCode code) {
		this(code, null);
	}

	public WalletException(BaseErrorCode code, Integer shortfall) {
		super(code);
		this.shortfall = shortfall;
	}
}

