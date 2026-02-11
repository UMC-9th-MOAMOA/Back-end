package com.example.moamoa_backend.domain.attendance.dto;

import java.time.LocalDate;

public class AttendanceResponseDto {

	public record CheckInResult(
		LocalDate date,
		boolean attendedToday,
		int streak,
		boolean completed7
	) {
	}
}
