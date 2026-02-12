package com.example.moamoa_backend.global.apiPayload.response;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import com.example.moamoa_backend.global.apiPayload.code.BaseSuccessCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public class ApiResponse<T> {

	@JsonProperty("isSuccess")
	private final boolean success;

	@JsonProperty("code")
	private final String code;

	@JsonProperty("message")
	private final String message;

	@JsonProperty("result")
	private T result;

	//성공
	public static <T> ApiResponse<T> onSuccess(BaseSuccessCode code, T result) {
		return new ApiResponse<>(true, code.getCode(), code.getMessage(), result);
	}

	//실패
	public static <T> ApiResponse<T> onFailure(BaseErrorCode code, T result) {
		return new ApiResponse<>(false, code.getCode(), code.getMessage(), result);
	}
}
