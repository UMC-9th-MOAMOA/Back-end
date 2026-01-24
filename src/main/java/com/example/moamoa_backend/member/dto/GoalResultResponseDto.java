package com.example.moamoa_backend.member.dto;

import java.time.LocalDate;

import com.example.moamoa_backend.member.entity.GoalResult;
import com.example.moamoa_backend.member.enums.GoalResultStatus;
import com.example.moamoa_backend.member.enums.GoalResultType;

public record GoalResultResponseDto(
	LocalDate goalDate,
	GoalResultType goalType,
	Integer targetCount,
	Integer achievedCount,
	GoalResultStatus status
) {
	public static GoalResultResponseDto from(GoalResult goalResult) {
		return new GoalResultResponseDto(
			goalResult.getGoalDate(),
			goalResult.getGoalType(),
			goalResult.getTargetCount(),
			goalResult.getAchievedCount(),
			goalResult.getStatus()
		);
	}
}
