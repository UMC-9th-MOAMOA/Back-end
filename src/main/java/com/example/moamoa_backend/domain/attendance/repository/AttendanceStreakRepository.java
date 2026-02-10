package com.example.moamoa_backend.attendance.repository;

import com.example.moamoa_backend.attendance.entity.AttendanceStreak;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AttendanceStreakRepository extends JpaRepository<AttendanceStreak, Long> {

    /**
     * streak 업데이트는 동시성 꼬임 방지를 위해 락 추천
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from AttendanceStreak s where s.member.id = :memberId")
    Optional<AttendanceStreak> findByMemberIdForUpdate(@Param("memberId") Long memberId);

    Optional<AttendanceStreak> findByMember_Id(Long memberId);
}
