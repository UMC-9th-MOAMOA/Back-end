package com.example.moamoa_backend.policy.repository;

import com.example.moamoa_backend.policy.entity.MemberPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberPolicyRepository extends JpaRepository<MemberPolicy,Long> {
    Optional<MemberPolicy> findByMemberIdAndPolicyId(Long memberId, Long policyId);

    @Query("SELECT mp FROM MemberPolicy mp JOIN FETCH mp.policy WHERE mp.member.id = :memberId")
    List<MemberPolicy> findAllByMemberId(@Param("memberId") Long memberId);
}
