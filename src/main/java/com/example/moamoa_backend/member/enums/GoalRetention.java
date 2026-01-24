package com.example.moamoa_backend.member.enums;

import java.time.LocalDate;

public enum GoalRetention {
	CONTINUE,
	ONE_WEEK,
	TWO_WEEKS,
	ONE_MONTH;

	public LocalDate calculateEndDate(LocalDate startDate) {
		return switch (this) {
			case CONTINUE -> null;
			case ONE_WEEK -> startDate.plusWeeks(1).minusDays(1);
			case TWO_WEEKS -> startDate.plusWeeks(2).minusDays(1);
			case ONE_MONTH -> startDate.plusMonths(1).minusDays(1);
		};
	}
}