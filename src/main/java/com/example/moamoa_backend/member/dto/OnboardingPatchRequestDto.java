package com.example.moamoa_backend.member.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

public record OnboardingPatchRequestDto(
	@Valid
	List<Selection> selections,   // scope=INTERESTS/ALL에서 사용 (그 외 null 허용)

	@Min(value = 0, message = "일일 목표는 0~5 사이여야 합니다.")
	@Max(value = 5, message = "일일 목표는 0~5 사이여야 합니다.")
	Integer dailyMissionGoal      // scope=GOAL/ALL에서 사용 (그 외 null 허용)
) {
	public record Selection(
		Long interestId,
		List<Long> subInterestIds
	) {}
}
