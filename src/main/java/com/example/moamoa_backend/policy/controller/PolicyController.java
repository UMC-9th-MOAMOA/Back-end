package com.example.moamoa_backend.policy.controller;

import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.policy.dto.res.PolicyResDto;
import com.example.moamoa_backend.policy.entity.Policy;
import com.example.moamoa_backend.policy.exception.code.PolicySuccessCode;
import com.example.moamoa_backend.policy.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @Operation(summary = "약관 상세 조회", description = "약관 ID(policyId)를 이용해 약관의 상세 내용(본문 포함)을 조회합니다.")
    @SecurityRequirements(value = {})
    @Parameter(name = "policyId", description = "조회할 약관의 ID")
    @GetMapping("/{policyId}")
    public ApiResponse<PolicyResDto.DetailDto> getPolicyDetail(
            @PathVariable Long policyId
    ) {
        PolicyResDto.DetailDto result = policyService.getPolicyDetail(policyId);
        return ApiResponse.onSuccess(PolicySuccessCode.POLICY_GET_SUCCESS, result);
    }

    @Operation(summary = "활성 약관 목록 조회", description = "회원가입 시 필요한 활성화된 약관 목록을 조회합니다.")
    @SecurityRequirements(value = {})
    @GetMapping()
    public ApiResponse<List<PolicyResDto.SignupDto>> getActivePolicies() {
        return ApiResponse.onSuccess(PolicySuccessCode.POLICY_LIST_GET_SUCCESS, policyService.getActivePolicies());
    }
}
