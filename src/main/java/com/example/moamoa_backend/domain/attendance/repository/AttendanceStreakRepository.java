package com.example.moamoa_backend.domain.attendance.repository;

import com.example.moamoa_backend.domain.attendance.entity.AttendanceStreak;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AttendanceStreakRepository extends JpaRepository<AttendanceStreak, Long> {

	/**
	 * streak 업데이트시 동시성 방지 위한 락 처리
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from AttendanceStreak s where s.member.id = :memberId")
	Optional<AttendanceStreak> findByMemberIdForUpdate(@Param("memberId") Long memberId);

	Optional<AttendanceStreak> findByMember_Id(Long memberId);
}
