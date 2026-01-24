package com.example.moamoa_backend.member.repository;

import com.example.moamoa_backend.member.entity.Member;
import com.example.moamoa_backend.member.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member,Long> {

    boolean existsByEmailAndProvider(String email, Provider provider);

    Optional<Member> findByEmailAndProvider(String email, Provider provider);

    Optional<Member> findByProviderAndProviderId(Provider provider, String providerId);

    @Query("""
        select m
        from Member m
        where (m.pendingApplyDate is not null and m.pendingApplyDate <= :today)
           or (m.goalEndDate is not null and m.goalEndDate < :today)
        """)
    List<Member> findMembersForGoalUpdate(LocalDate today);

    @Query("""
        select m
        from Member m
        where m.dailyGoal is not null
        """)
    List<Member> findMembersWithDailyGoal();

    @Query("""
        select m
        from Member m
        where m.weeklyGoal is not null
        """)
    List<Member> findMembersWithWeeklyGoal();
}