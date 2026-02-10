package com.example.moamoa_backend.domain.policy.service;

import com.example.moamoa_backend.domain.member.entity.Member;
import com.example.moamoa_backend.domain.member.exception.MemberException;
import com.example.moamoa_backend.domain.member.exception.code.MemberErrorCode;
import com.example.moamoa_backend.domain.member.repository.MemberRepository;
import com.example.moamoa_backend.domain.policy.dto.req.PolicyReqDto;
import com.example.moamoa_backend.domain.policy.dto.res.PolicyResDto;
import com.example.moamoa_backend.domain.policy.entity.MemberPolicy;
import com.example.moamoa_backend.domain.policy.entity.Policy;
import com.example.moamoa_backend.domain.policy.exception.PolicyException;
import com.example.moamoa_backend.domain.policy.exception.code.PolicyErrorCode;
import com.example.moamoa_backend.domain.policy.repository.MemberPolicyRepository;
import com.example.moamoa_backend.domain.policy.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final MemberPolicyRepository memberPolicyRepository;
    private final MemberRepository memberRepository;

    /**
     * 약관 상세 조회 (내용 포함)
     * @return 활성화된 약관 목록 (필수 우선 정렬)
     */
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

    /**
     * 약관 간단 조회 (내용 미포함)
     * @return 활성화된 약관 목록 (필수 우선 정렬)
     */
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

    /**
     * 약관 동의 생성 (회원가입 시)
     *
     * @param member 가입하는 회원
     * @param requests 약관 동의 요청 목록
     */
    @Transactional
    public void createPolicyAgreements(Member member, List<PolicyReqDto.AgreementDto> requests) {

        // 활성 약관 전체 조회
        List<Policy> allPolicies = policyRepository.findAllByIsActiveTrueOrderByIsMandatoryDescIdAsc();

        // 요청 약관에 중복이 존재하는지 체크
        validateDuplicatePolicyIds(requests);

        // 요청 데이터 Map 변환
        Map<Long, Boolean> requestMap = requests.stream()
                .collect(Collectors.toMap(PolicyReqDto.AgreementDto::policyId, PolicyReqDto.AgreementDto::isAgreed));

        // 약관 존재 검증, 필수 약관 동의 검증
        validatePolicyIds(allPolicies, requestMap.keySet());
        validateMandatoryPolicies(allPolicies, requestMap);

        // 기존 내역 조회 없이 바로 엔티티 생성 및 저장
        List<MemberPolicy> newPolicies = allPolicies.stream()
                .filter(policy -> requestMap.getOrDefault(policy.getId(), false))
                .map(policy -> MemberPolicy.builder()
                        .member(member)
                        .policy(policy)
                        .isAgreed(true)
                        .agreedAt(LocalDateTime.now())
                        .build())
                .toList();

        memberPolicyRepository.saveAll(newPolicies);
    }

    /**
     * 약관 동의 수정 (가입 이후)
     *
     * @param memberId 회원 ID
     * @param requests 약관 동의 요청 목록
     */
    @Transactional
    public void updatePolicyAgreements(Long memberId, List<PolicyReqDto.AgreementDto> requests) {

        // 멤버 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 활성 약관 전체 조회
        List<Policy> allPolicies = policyRepository.findAllByIsActiveTrueOrderByIsMandatoryDescIdAsc();

        // 요청 약관에 중복이 존재하는지 체크
        validateDuplicatePolicyIds(requests);

        // 요청 데이터 Map 변환
        Map<Long, Boolean> requestMap = requests.stream()
                .collect(Collectors.toMap(PolicyReqDto.AgreementDto::policyId, PolicyReqDto.AgreementDto::isAgreed));

        // 약관 존재 검증, 필수 약관 동의 검증
        validatePolicyIds(allPolicies, requestMap.keySet());
        validateMandatoryPolicies(allPolicies, requestMap);

        // 기존 내역 조회
        List<MemberPolicy> existingPolicies = memberPolicyRepository.findAllByMemberId(member.getId());

        // 기존 내역 Map 변환
        Map<Long, MemberPolicy> existingMap = existingPolicies.stream()
                .collect(Collectors.toMap(mp -> mp.getPolicy().getId(), mp -> mp));

        List<MemberPolicy> toSave = new ArrayList<>();

        // 비교 및 업데이트
        for (Policy policy : allPolicies) {
            // 요청 리스트에 있는 약관만 처리
            if (requestMap.containsKey(policy.getId())) {
                boolean newStatus = requestMap.get(policy.getId());

                if (existingMap.containsKey(policy.getId())) {

                    MemberPolicy existing = existingMap.get(policy.getId());
                    if (existing.isAgreed() != newStatus) {
                        existing.updateAgreement(newStatus);
                    }

                } else {

                    toSave.add(MemberPolicy.builder()
                            .member(member)
                            .policy(policy)
                            .isAgreed(newStatus)
                            .agreedAt(newStatus ? LocalDateTime.now() : null) // false면 null, true면 now
                            .build());
                }
            }
        }

        if (!toSave.isEmpty()) {
            memberPolicyRepository.saveAll(toSave);
        }

        // TODO: 관심사 분리 필요 - PolicyService에서 Member 상태 변경은 부적절하다는 피드백
        //       Facade 패턴 등을 활용하여 상위 레이어에서 처리하도록 리팩토링 예정
        member.completePolicyAgreement();
    }


    // ======================= Helper Methods =======================


    /**
     * 필수 약관 동의 여부 검증
     * @throws PolicyException 필수 약관 미동의 시
     */
    private void validateMandatoryPolicies(List<Policy> allPolicies, Map<Long, Boolean> requestMap) {
        List<Policy> mandatoryPolicies = allPolicies.stream()
                .filter(Policy::isMandatory)
                .toList();

        for (Policy mandatory : mandatoryPolicies) {
            Boolean isAgreed = requestMap.get(mandatory.getId());
            if (isAgreed == null || !isAgreed) {
                throw new PolicyException(PolicyErrorCode.MANDATORY_AGREEMENT_REQUIRED);
            }
        }
    }

    /**
     * 요청된 약관 ID 존재 여부 검증
     * @throws PolicyException 존재하지 않는 약관 ID 포함 시
     */
    private void validatePolicyIds(List<Policy> allPolicies, Set<Long> requestedPolicyIds) {
        Set<Long> validIds = allPolicies.stream()
                .map(Policy::getId)
                .collect(Collectors.toSet());

        Set<Long> invalidIds = requestedPolicyIds.stream()
                .filter(reqId -> !validIds.contains(reqId))
                .collect(Collectors.toSet());

        if (!invalidIds.isEmpty()) {
            throw new PolicyException(PolicyErrorCode.POLICY_NOT_FOUND);
        }
    }

    /**
     * 중복 약관 ID 검증
     * @throws PolicyException 중복된 약관 ID 포함 시
     */
    private void validateDuplicatePolicyIds(List<PolicyReqDto.AgreementDto> requests) {
        Set<Long> policyIds = new HashSet<>();
        for (PolicyReqDto.AgreementDto request : requests) {
            if (!policyIds.add(request.policyId())) {
                throw new PolicyException(PolicyErrorCode.DUPLICATE_POLICY_ID);
            }
        }
    }
}
