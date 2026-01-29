package com.example.moamoa_backend.item.dto;

import com.example.moamoa_backend.member.enums.GoalRetention;

import java.time.LocalDate;
import java.util.List;

public class HomePocketResponseDto {
    public record Response(
            long todayMissionMinutes,
            long thisWeekMissionMinutes,
            int walletPoint,
            int currentStreak,
            GoalProgress goalProgress // 목표 미설정이면 null
    ) {}

    public record GoalProgress(
            Integer dailyGoal,
            Long lastWeekTotalMissionCount,
            Long thisWeekTotalMissionCount,
            List<Long> thisWeekDailyMissionCounts // 월~일 항상 7칸, null 없음
    ) {}
}
