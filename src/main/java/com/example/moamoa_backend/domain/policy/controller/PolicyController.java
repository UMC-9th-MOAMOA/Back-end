package com.example.moamoa_backend.domain.policy.controller;

import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.domain.policy.dto.req.PolicyReqDto;
import com.example.moamoa_backend.domain.policy.dto.res.PolicyResDto;
import com.example.moamoa_backend.domain.policy.exception.code.PolicySuccessCode;
import com.example.moamoa_backend.domain.policy.service.PolicyService;
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
@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
public class PolicyController  implements PolicyControllerDocs {

    private final PolicyService policyService;

    @GetMapping("/details")
    public ApiResponse<List<PolicyResDto.DetailDto>> getDetailPolicies() {
        return ApiResponse.onSuccess(PolicySuccessCode.POLICY_LIST_GET_SUCCESS, policyService.getDetailPolicies());
    }

    @GetMapping()
    public ApiResponse<List<PolicyResDto.SimpleDto>> getPolicies() {
        return ApiResponse.onSuccess(PolicySuccessCode.POLICY_LIST_GET_SUCCESS, policyService.getSimplePolicies());
    }

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
