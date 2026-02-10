package com.example.moamoa_backend.domain.member.enums;

import java.time.LocalDate;

public enum GoalRetention {
	CONTINUE,
	ONE_WEEK,
	TWO_WEEKS,
	ONE_MONTH;


	/**
	 * 목표 시작일 기준으로 유지 종료일(포함)을 계산한다.
	 * CONTINUE는 종료일이 없으므로 null을 반환한다.
	 */
	public LocalDate calculateEndDate(LocalDate startDate) {
		return switch (this) {
			case CONTINUE -> null;
			case ONE_WEEK -> startDate.plusWeeks(1).minusDays(1);
			case TWO_WEEKS -> startDate.plusWeeks(2).minusDays(1);
			case ONE_MONTH -> startDate.plusMonths(1).minusDays(1);
		};
	}
}