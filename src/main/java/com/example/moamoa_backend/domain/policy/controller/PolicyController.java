package com.example.moamoa_backend.domain.policy.controller;

import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.domain.policy.dto.req.PolicyReqDto;
import com.example.moamoa_backend.domain.policy.dto.res.PolicyResDto;
import com.example.moamoa_backend.domain.policy.exception.code.PolicySuccessCode;
import com.example.moamoa_backend.domain.policy.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 약관(Policy) 관련 API 컨트롤러
 * - 회원가입 시 필요한 약관 조회
 * - 사용자의 약관 동의 내역 관리
 */
@Tag(name = "Policy", description = "약관 관련 API")
@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @Operation(summary = "약관 상세 조회", description = "회원가입 시 필요한 활성화된 약관의 상세정보를 조회합니다.")
    @SecurityRequirements(value = {})
    @GetMapping("/details")
    public ApiResponse<List<PolicyResDto.DetailDto>> getDetailPolicies() {
        return ApiResponse.onSuccess(PolicySuccessCode.POLICY_LIST_GET_SUCCESS, policyService.getDetailPolicies());
    }

    @Operation(summary = "약관 간단 조회", description = "회원가입 시 필요한 활성화된 약관의 기본정보를 조회합니다.")
    @SecurityRequirements(value = {})
    @GetMapping()
    public ApiResponse<List<PolicyResDto.SimpleDto>> getPolicies() {
        return ApiResponse.onSuccess(PolicySuccessCode.POLICY_LIST_GET_SUCCESS, policyService.getSimplePolicies());
    }

    @Operation(summary = "약관 동의 내역 수정", description = "로그인한 사용자의 약관 동의 내역을 수정합니다.")
    @PutMapping("/agreements")
    public ApiResponse<Void> updateAgreements(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid PolicyReqDto.AgreementListDto request
    ) {
        Long memberId = Long.parseLong(userDetails.getUsername());
        policyService.updatePolicyAgreements(memberId, request.agreements());
        return ApiResponse.onSuccess(PolicySuccessCode.POLICY_AGREEMENT_UPDATE_SUCCESS, null);
    }
}
