package com.example.moamoa_backend.member.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record OnboardingPatchRequestDto(
	@Valid
	List<Selection> selections,   // scope=INTERESTS/ALL에서 사용 (GOAL에서는 null 허용)

	@Min(value = 0, message = "일일 목표는 0~5 사이여야 합니다.")
	@Max(value = 5, message = "일일 목표는 0~5 사이여야 합니다.")
	Integer dailyMissionGoal      // scope=GOAL/ALL에서 사용 (INTERESTS에서는 null 허용)
) {
	public record Selection(
		Long interestId,            // 대분류 ID (소속 검증에 사용)
		List<Long> subInterestIds   // 세부 관심사 ID 목록 (실제 저장 대상)
	) {}
}
