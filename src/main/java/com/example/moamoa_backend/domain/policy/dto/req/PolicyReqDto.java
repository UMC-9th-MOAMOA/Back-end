package com.example.moamoa_backend.domain.policy.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 정책 관련 Request DTO
 */
public class PolicyReqDto {

	/**
	 * 약관 동의 목록
	 */
	public record AgreementListDto(
		@NotNull
		List<@Valid @NotNull AgreementDto> agreements
	) {
	}

	/**
	 * 개별 약관 동의
	 */
	public record AgreementDto(
		@NotNull(message = "약관 ID는 필수입니다.")
		Long policyId,

		@NotNull(message = "동의 여부는 필수입니다.")
		Boolean isAgreed
	) {
	}
}
