package com.example.moamoa_backend.member.dto.res;


import java.time.LocalDate;
import java.util.List;

import com.example.moamoa_backend.member.entity.GoalResult;
import com.example.moamoa_backend.member.enums.GoalResultStatus;
import com.example.moamoa_backend.member.enums.GoalResultType;

public record GoalPopupResponseDto(
	List<Popup> popups
) {
	public record Popup(
		Long goalResultId,
		GoalResultType goalType,
		LocalDate goalDate,
		Integer targetCount,
		Integer achievedCount,
		GoalResultStatus status
	) {
		public static Popup from(GoalResult gr) {
			return new Popup(
				gr.getId(),
				gr.getGoalType(),
				gr.getGoalDate(),
				gr.getTargetCount(),
				gr.getAchievedCount(),
				gr.getStatus()
			);
		}
	}
}
