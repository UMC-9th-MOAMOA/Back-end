package com.example.moamoa_backend.domain.item.dto;

import java.util.List;

public class HomePocketResponseDto {
	public record Response(
		String name,
		long todayMissionMinutes,
		long thisWeekMissionMinutes,
		int walletPoint,
		int currentStreak,
		GoalProgress goalProgress // 목표 미설정이면 null
	) {
	}

	public record GoalProgress(
		Integer dailyGoal,
		Long lastWeekTotalMissionCount,
		Long thisWeekTotalMissionCount,
		List<Long> thisWeekDailyMissionCounts // 월~일 항상 7칸, null 없음
	) {
	}
}
