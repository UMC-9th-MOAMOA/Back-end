package com.example.moamoa_backend.policy.controller;

import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.policy.dto.res.PolicyResDto;
import com.example.moamoa_backend.policy.exception.code.PolicySuccessCode;
import com.example.moamoa_backend.policy.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @Operation(summary = "약관 상세 조회", description = "회원가입 시 필요한 활성화된 약관을 상세 조회합니다.")
    @SecurityRequirements(value = {})
    @GetMapping("/details")
    public ApiResponse<List<PolicyResDto.DetailDto>> getDetailPolicies() {
        return ApiResponse.onSuccess(PolicySuccessCode.POLICY_LIST_GET_SUCCESS, policyService.getDetailPolicies());
    }

    @Operation(summary = "약관 간단 조회", description = "회원가입 시 필요한 활성화된 약관 목록을 조회합니다.")
    @SecurityRequirements(value = {})
    @GetMapping()
    public ApiResponse<List<PolicyResDto.SimpleDto>> getPolicies() {
        return ApiResponse.onSuccess(PolicySuccessCode.POLICY_LIST_GET_SUCCESS, policyService.getSimplePolicies());
    }
}
