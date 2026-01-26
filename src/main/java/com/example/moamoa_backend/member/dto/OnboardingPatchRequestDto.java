package com.example.moamoa_backend.member.dto;

import java.util.List;

import com.example.moamoa_backend.member.enums.GoalRetention;

import jakarta.validation.Valid;

public record OnboardingPatchRequestDto(
	@Valid
	List<Selection> selections,   // scope=INTERESTS/ALL에서 사용 (GOAL에서는 null 허용)

	Integer dailyMissionGoal,     // 0~5 또는 null(OFF/나중에 설정)
	GoalRetention goalRetention   // null 가능 (dailyMissionGoal이 null이면 반드시 null이어야 함: 서비스에서 검증)
) {
	public record Selection(
		Long interestId,            // 대분류 ID (소속 검증에 사용)
		List<Long> subInterestIds   // 세부 관심사 ID 목록 (실제 저장 대상)
	) {}
}
