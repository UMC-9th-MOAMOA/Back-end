package com.example.moamoa_backend.member.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL) // scope 응답에서 null 필드는 JSON에서 제외
public record OnboardingResponseDto(
	List<Selection> selections,    // scope=INTERESTS/ALL에서 내려줄 수 있음
	Integer dailyMissionGoal       // scope=GOAL/ALL에서 내려줄 수 있음
) {
	public record Selection(Long interestId, List<Long> subInterestIds) {}

	// scope별 응답을 쉽게 만들기 위한 팩토리 메서드
	public static OnboardingResponseDto of(List<Selection> selections, Integer dailyMissionGoal) {
		return new OnboardingResponseDto(selections, dailyMissionGoal);
	}
}
