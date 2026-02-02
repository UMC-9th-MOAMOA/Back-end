package com.example.moamoa_backend.policy.repository;

import com.example.moamoa_backend.policy.entity.MemberPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 회원-약관 동의 정보 Repository
 */
public interface MemberPolicyRepository extends JpaRepository<MemberPolicy,Long> {

    /**
     * 특정 회원의 모든 약관 동의 내역 조회
     * - Policy를 fetch join하여 N+1 방지
     * - Member는 ID만 비교하여 순환참조 방지
     *
     * @param memberId 회원 ID
     * @return 해당 회원의 약관 동의 목록
     */
    @Query("SELECT mp FROM MemberPolicy mp JOIN FETCH mp.policy WHERE mp.member.id = :memberId")
    List<MemberPolicy> findAllByMemberId(@Param("memberId") Long memberId);
}
