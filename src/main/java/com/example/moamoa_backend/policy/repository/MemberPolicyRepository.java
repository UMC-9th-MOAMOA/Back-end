package com.example.moamoa_backend.policy.repository;

import com.example.moamoa_backend.policy.entity.MemberPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberPolicyRepository extends JpaRepository<MemberPolicy,Long> {
    Optional<MemberPolicy> findByMemberIdAndPolicyId(Long memberId, Long policyId);
    List<MemberPolicy> findAllByMemberId(Long id);
}
