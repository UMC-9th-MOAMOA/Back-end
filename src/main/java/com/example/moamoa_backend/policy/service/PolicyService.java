package com.example.moamoa_backend.policy.service;

import com.example.moamoa_backend.policy.dto.res.PolicyResDto;
import com.example.moamoa_backend.policy.entity.Policy;
import com.example.moamoa_backend.policy.exception.PolicyException;
import com.example.moamoa_backend.policy.exception.code.PolicyErrorCode;
import com.example.moamoa_backend.policy.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyService {

    private final PolicyRepository policyRepository;

    public PolicyResDto.DetailDto getPolicyDetail(Long policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new PolicyException(PolicyErrorCode.POLICY_NOT_FOUND)); // 예외 처리 필수

        return PolicyResDto.DetailDto.builder()
                .id(policy.getId())
                .policyType(policy.getPolicyType().name())
                .isMandatory(policy.isMandatory())
                .version(policy.getVersion())
                .title(policy.getTitle())
                .content(policy.getContent())
                .isActive(policy.isActive())
                .effectiveAt(policy.getEffectiveAt())
                .build();
    }

    public List<PolicyResDto.SignupDto> getActivePolicies() {
        List<Policy> policies = policyRepository.findAllByIsActiveTrue();

        return policies.stream()
                .map(policy -> PolicyResDto.SignupDto.builder()
                        .id(policy.getId())
                        .title(policy.getTitle())
                        .content(policy.getContent())
                        .isMandatory(policy.isMandatory())
                        .build())
                .toList();
    }

}
