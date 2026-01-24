package com.example.moamoa_backend.member.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.moamoa_backend.member.entity.Member;
import com.example.moamoa_backend.member.enums.GoalRetention;
import com.example.moamoa_backend.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoalMaintenanceService {

	private final MemberRepository memberRepository;
	private final GoalResultService goalResultService;

	@Transactional
	public void applyGoalStateIfNeeded(Member member, LocalDate today) {
		applyPendingIfDue(member, today);
		expireGoalIfNeeded(member, today);
	}

	@Transactional
	// 하루 마감 직후(00:01)에 전일/전주 결과 확정 및 목표 반영
	@Scheduled(cron = "0 1 0 * * *")
	public void processDueGoals() {
		LocalDate today = LocalDate.now();
		goalResultService.recordDailyResults(today.minusDays(1));
		if (today.getDayOfWeek() == DayOfWeek.MONDAY) {
			goalResultService.recordWeeklyResults(today.minusDays(1));
		}
		List<Member> members = memberRepository.findMembersForGoalUpdate(today);
		for (Member member : members) {
			applyGoalStateIfNeeded(member, today);
		}
	}

	public LocalDate resolveApplyDate(LocalDate today) {
		if (today.getDayOfWeek() == DayOfWeek.MONDAY) {
			return today;
		}
		return today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
	}

	private void applyPendingIfDue(Member member, LocalDate today) {
		LocalDate pendingApplyDate = member.getPendingApplyDate();
		if (pendingApplyDate == null || pendingApplyDate.isAfter(today)) {
			return;
		}

		GoalRetention pendingRetention = member.getPendingGoalRetention();
		member.applyGoalSetting(member.getPendingDailyGoal(), pendingRetention, pendingApplyDate);
		member.clearPendingGoalSetting();
	}

	private void expireGoalIfNeeded(Member member, LocalDate today) {
		LocalDate endDate = member.getGoalEndDate();
		if (endDate != null && today.isAfter(endDate)) {
			member.applyGoalSetting(null, null, today);
		}
	}
}
