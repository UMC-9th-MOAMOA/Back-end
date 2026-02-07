package com.example.moamoa_backend.global.apiPayload.handler;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import com.example.moamoa_backend.global.apiPayload.code.GeneralErrorCode;
import com.example.moamoa_backend.global.apiPayload.exception.GeneralException;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.global.security.jwt.exception.code.JwtErrorCode;
import com.example.moamoa_backend.item.exception.code.ItemErrorCode;
import com.example.moamoa_backend.member.exception.code.MemberErrorCode;
import com.example.moamoa_backend.wallet.exception.WalletException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GeneralExceptionAdvice {

	/**
	 *   GeneralException 처리
	 */
	@ExceptionHandler(GeneralException.class)
	public ResponseEntity<ApiResponse<Void>> handleGeneralException(GeneralException e) {

		return ResponseEntity.status(e.getCode().getStatus())
			.body(ApiResponse.onFailure(
					e.getCode(),
					null
				)
			);
	}

	/**
	 * walletExcetpion 처리
	 */
	@ExceptionHandler(WalletException.class)
	public ResponseEntity<ApiResponse<Map<String, Integer>>> handleWalletException(WalletException e) {

		Map<String, Integer> result = null;

		// 포인트 부족일 때만 shortfall 내려주기
		if (e.getShortfall() != null) {
			result = Map.of("shortfall", e.getShortfall());
		}

		return ResponseEntity.status(e.getCode().getStatus())
			.body(ApiResponse.onFailure(e.getCode(), result));
	}

	/**
	 * @CookieValue(required=true) 검증 실패 시 (쿠키 누락)
	 */
	@ExceptionHandler(MissingRequestCookieException.class)
	public ResponseEntity<ApiResponse<Void>> handleMissingRequestCookieException(MissingRequestCookieException e) {

		log.warn("필수 쿠키 누락: {}", e.getCookieName());

		return ResponseEntity
			.status(JwtErrorCode.REFRESH_TOKEN_MISSING.getStatus())
			.body(ApiResponse.onFailure(JwtErrorCode.REFRESH_TOKEN_MISSING, null));
	}

	/**
	 * @RequestParam, @PathVariable 등에서 타입 변환 실패할 때 발생
	 * 예) ?scope=AAA (OnboardingUpdateScope로 변환 실패)
	 */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {

		// scope 파라미터에 대해서만 명시적으로 INVALID_SCOPE로 내려줌
		if ("scope".equals(e.getName()) && e.getRequiredType() != null && e.getRequiredType().isEnum()) {
			return ResponseEntity
				.status(MemberErrorCode.INVALID_SCOPE.getStatus())
				.body(ApiResponse.onFailure(MemberErrorCode.INVALID_SCOPE, null));
		}

		//  /items?category=... (ItemCategory enum 파싱 실패)
		if ("category".equals(e.getName())) {
			return ResponseEntity
				.status(ItemErrorCode.ITEM_INVALID_CATEGORY.getStatus())
				.body(ApiResponse.onFailure(ItemErrorCode.ITEM_INVALID_CATEGORY, null));
		}

		//  /items?type=... (ItemType enum 파싱 실패)
		if ("type".equals(e.getName())) {
			return ResponseEntity
				.status(ItemErrorCode.ITEM_INVALID_TYPE.getStatus())
				.body(ApiResponse.onFailure(ItemErrorCode.ITEM_INVALID_TYPE, null));
		}

		// scope 외 다른 파라미터 타입 미스매치는 일단 500이 아니라 400으로 내림
		BaseErrorCode errorCode = GeneralErrorCode.BAD_REQUEST;
		log.warn("Type mismatch: param={}, value={}, requiredType={}",
			e.getName(), e.getValue(), e.getRequiredType());

		return ResponseEntity
			.status(errorCode.getStatus())
			.body(ApiResponse.onFailure(errorCode, null));
	}

	/**
	 *   예상하지 못한 모든 예외 처리
	 */
	// 검증/바인딩 실패는 400으로 내려서 필드별 오류를 result에 담는다.
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException e) {

		Map<String, String> errors = extractFieldErrors(e.getBindingResult().getFieldErrors());

		return ResponseEntity
			.status(GeneralErrorCode.METHOD_ARGUMENT_NOT_VALID.getStatus())
			.body(ApiResponse.onFailure(GeneralErrorCode.METHOD_ARGUMENT_NOT_VALID, errors));
	}

	@ExceptionHandler(BindException.class)
	public ResponseEntity<ApiResponse<Map<String, String>>> handleBindException(BindException e) {

		Map<String, String> errors = extractFieldErrors(e.getBindingResult().getFieldErrors());

		return ResponseEntity
			.status(GeneralErrorCode.BINDING_ERROR.getStatus())
			.body(ApiResponse.onFailure(GeneralErrorCode.BINDING_ERROR, errors));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationException(
		ConstraintViolationException e) {

		Map<String, String> errors = new LinkedHashMap<>();
		for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
			String field = resolveViolationField(violation);
			String message = violation.getMessage();
			if (field != null && message != null) {
				errors.putIfAbsent(field, message);
			}
		}

		return ResponseEntity
			.status(GeneralErrorCode.CONSTRAINT_VIOLATION.getStatus())
			.body(ApiResponse.onFailure(GeneralErrorCode.CONSTRAINT_VIOLATION, errors));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Map<String, String>>> handleHttpMessageNotReadableException(
		HttpMessageNotReadableException e) {

		Map<String, String> errors = new LinkedHashMap<>();
		Throwable cause = e.getMostSpecificCause();
		String detailMessage = cause != null ? cause.getMessage() : e.getMessage();
		if (detailMessage != null) {
			log.warn("Request body parse error: {}", detailMessage, e);
		} else {
			log.warn("Request body parse error", e);
		}
		errors.put("body", "요청 본문을 읽을 수 없습니다");

		return ResponseEntity
			.status(GeneralErrorCode.MESSAGE_NOT_READABLE.getStatus())
			.body(ApiResponse.onFailure(GeneralErrorCode.MESSAGE_NOT_READABLE, errors));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
		BaseErrorCode errorCode = GeneralErrorCode.INTERNAL_SERVER_ERROR;

		log.error("예상치 못한 에러 발생: ", e);

		return ResponseEntity
			.status(errorCode.getStatus())
			.body(ApiResponse.onFailure(errorCode, null));
	}

	// BindingResult의 FieldError 목록을 field -> message로 정리한다.
	private Map<String, String> extractFieldErrors(List<FieldError> fieldErrors) {
		Map<String, String> errors = new LinkedHashMap<>();
		for (FieldError fieldError : fieldErrors) {
			String field = fieldError.getField();
			String message = fieldError.getDefaultMessage();
			if (field != null && message != null) {
				errors.putIfAbsent(field, message);
			}
		}
		return errors;
	}

	// ConstraintViolation 경로에서 마지막 노드명을 필드로 사용한다.
	private String resolveViolationField(ConstraintViolation<?> violation) {
		String field = null;
		Path path = violation.getPropertyPath();
		if (path != null) {
			for (Path.Node node : path) {
				if (node.getName() != null) {
					field = node.getName();
				}
			}
		}
		return field != null ? field : "param";
	}
}

