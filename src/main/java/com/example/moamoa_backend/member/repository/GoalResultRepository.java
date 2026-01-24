package com.example.moamoa_backend.member.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.moamoa_backend.member.entity.GoalResult;
import com.example.moamoa_backend.member.enums.GoalResultType;

@Repository
public interface GoalResultRepository extends JpaRepository<GoalResult, Long> {
	Optional<GoalResult> findByMemberIdAndGoalTypeAndGoalDate(
		Long memberId,
		GoalResultType goalType,
		LocalDate goalDate
	);
}
