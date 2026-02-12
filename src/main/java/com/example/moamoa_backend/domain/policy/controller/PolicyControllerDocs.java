package com.example.moamoa_backend.domain.policy.controller;

import com.example.moamoa_backend.domain.policy.dto.req.PolicyReqDto;
import com.example.moamoa_backend.domain.policy.dto.res.PolicyResDto;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 약관(Policy) 관련 API 문서
 * - 약관 조회 (상세/간단)
 * - 약관 동의 내역 수정
 */
@Tag(name = "Policy API", description = "약관 관련 API")
public interface PolicyControllerDocs {

	@Operation(
		summary = "약관 상세 조회",
		description = """
			활성화된 약관의 상세 정보를 조회합니다.<br>
			인증 없이 호출할 수 있으며, 회원가입 화면에서 약관 내용을 표시할 때 사용합니다.<br>
			
			**[응답 정보]**
			
			약관 ID, 제목, 본문 내용, 필수 여부가 포함됩니다.<br>
			필수 약관이 먼저 정렬되어 반환됩니다.<br>
			"""
	)
	@SecurityRequirements(value = {})
	ApiResponse<List<PolicyResDto.DetailDto>> getDetailPolicies();

	@Operation(
		summary = "약관 간단 조회",
		description = """
			활성화된 약관의 기본 정보를 조회합니다.<br>
			인증 없이 호출할 수 있으며, 약관 동의 체크리스트를 구성할 때 사용합니다.<br>
			
			**[응답 정보]**
			
			약관 ID, 제목, 필수 여부가 포함됩니다.<br>
			본문 내용은 포함되지 않습니다.<br>
			필수 약관이 먼저 정렬되어 반환됩니다.<br>
			"""
	)
	@SecurityRequirements(value = {})
	ApiResponse<List<PolicyResDto.SimpleDto>> getPolicies();

	@Operation(
		summary = "약관 동의 내역 수정",
		description = """
			로그인한 사용자의 약관 동의 내역을 수정합니다.<br>
			
			**[인증 필요]**
			
			Header에 유효한 Access Token이 필요합니다.<br>
			Authorization: Bearer {accessToken}
			
			**[요청 조건]**
			
			약관 ID(policyId)와 동의 여부(isAgreed)를 포함한 목록을 전달해야 합니다.<br>
			활성화된 약관의 ID만 허용되며, 존재하지 않는 약관 ID는 오류가 반환됩니다.<br>
			동일한 약관 ID를 중복으로 전달할 수 없습니다.<br>
			
			**[동작 방식]**
			
			기존 동의 내역이 있으면 동의 상태를 업데이트합니다.<br>
			기존 동의 내역이 없으면 새로 생성합니다.<br>
			
			**[주의]**
			
			필수 약관은 반드시 동의(true)해야 합니다.<br>
			필수 약관에 미동의(false) 시 오류가 반환됩니다.<br>
			"""
	)
	ApiResponse<Void> updateAgreements(
		@AuthenticationPrincipal UserDetails userDetails,
		@RequestBody @Valid PolicyReqDto.AgreementListDto request
	);
}