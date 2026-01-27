package com.example.moamoa_backend.policy.service;

import com.example.moamoa_backend.policy.dto.res.PolicyResDto;
import com.example.moamoa_backend.policy.entity.Policy;
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

    public List<PolicyResDto.DetailDto> getDetailPolicies() {
        List<Policy> policies = policyRepository.findAllByIsActiveTrueOrderByIsMandatoryDescIdAsc();

        return policies.stream()
                .map(policy -> PolicyResDto.DetailDto.builder()
                        .id(policy.getId())
                        .title(policy.getTitle())
                        .content(policy.getContent())
                        .isMandatory(policy.isMandatory())
                        .build())
                .toList();
    }

    public List<PolicyResDto.SimpleDto> getSimplePolicies() {
        List<Policy> policies = policyRepository.findAllByIsActiveTrueOrderByIsMandatoryDescIdAsc();

        return policies.stream()
                .map(policy -> PolicyResDto.SimpleDto.builder()
                        .id(policy.getId())
                        .title(policy.getTitle())
                        .isMandatory(policy.isMandatory())
                        .build())
                .toList();
    }

}
