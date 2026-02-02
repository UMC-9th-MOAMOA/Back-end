package com.example.moamoa_backend.member.dto.res;

import java.util.List;

import java.time.LocalDate;

import com.example.moamoa_backend.member.enums.GoalRetention;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL) // scope 응답에서 null 필드는 JSON에서 제외
public record OnboardingResponseDto(
	List<Selection> selections,    // scope=INTERESTS/ALL에서 내려줄 수 있음
	Integer dailyMissionGoal,      // scope=GOAL/ALL에서 내려줄 수 있음
	GoalRetention goalRetention,   // 목표 유지 기간
	LocalDate goalEndDate,         // 목표 유지 종료일(포함)
	Integer pendingDailyMissionGoal, //다음 주 적용 대기 목표
	GoalRetention pendingGoalRetention, //다음 주 적용 대기 유지 시간
	LocalDate pendingApplyDate// 다음 주 적용 예정일(월요일)
) {
	public record Selection(Long interestId, List<Long> subInterestIds) {}

	// scope별 응답을 쉽게 만들기 위한 팩토리 메서드
		public static OnboardingResponseDto of(
			List<Selection> selections,
			Integer dailyMissionGoal,
			GoalRetention goalRetention,
			LocalDate goalEndDate,
			Integer pendingDailyMissionGoal,
			GoalRetention pendingGoalRetention,
			LocalDate pendingApplyDate
	) {
			return new OnboardingResponseDto(
				selections,
				dailyMissionGoal,
				goalRetention,
				goalEndDate,
				pendingDailyMissionGoal,
				pendingGoalRetention,
				pendingApplyDate
			);
		}
	}