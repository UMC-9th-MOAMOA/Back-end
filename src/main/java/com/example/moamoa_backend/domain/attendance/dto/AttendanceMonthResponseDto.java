package com.example.moamoa_backend.domain.attendance.dto;

import java.util.List;

public class AttendanceMonthResponseDto {
	/**
	 * attendedDays: 출석한 일자(1~31)
	 * missionRewardDays: 미션 보상으로 도토리 받은 일자(1~31) - WalletHistory.type == MISSION 기준
	 */
	public record Response(
		int year,
		int month,
		List<Integer> attendedDays,
		List<Integer> missionRewardDays
	) {
	}
}
