package com.example.moamoa_backend.domain.attendance.dto;

import java.util.List;

public class AttendanceMonthResponseDto {

	public record Response(
		int year,
		int month,
		List<Integer> attendedDays,
		List<Integer> missionRewardDays
	) {
	}
}
