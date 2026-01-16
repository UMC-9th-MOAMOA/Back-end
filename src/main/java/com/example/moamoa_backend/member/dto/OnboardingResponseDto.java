package com.example.moamoa_backend.member.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OnboardingResponseDto(
	List<Selection> selections,
	Integer dailyMissionGoal
) {
	public record Selection(Long interestId, List<Long> subInterestIds) {}

	public static OnboardingResponseDto of(List<Selection> selections, Integer dailyMissionGoal) {
		return new OnboardingResponseDto(selections, dailyMissionGoal);
	}
}
