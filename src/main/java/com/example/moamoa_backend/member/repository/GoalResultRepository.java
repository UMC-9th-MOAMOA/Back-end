package com.example.moamoa_backend.member.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

	// popupShown 처리 시 소유자 검증
	Optional<GoalResult> findByIdAndMemberId(Long id, Long memberId);

	// N+1 방지용 (배치에서 미리 존재하는 결과를 한 번에 조회)
	@Query("""
		select gr.member.id
		from GoalResult gr
		where gr.goalType = :goalType
		  and gr.goalDate = :goalDate
		  and gr.member.id in :memberIds
		""")
	List<Long> findExistingMemberIdsByGoalTypeAndGoalDate(
		@Param("goalType") GoalResultType goalType,
		@Param("goalDate") LocalDate goalDate,
		@Param("memberIds") List<Long> memberIds
	);
}
