package com.example.moamoa_backend.member.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
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
	private static final ZoneId KST = ZoneId.of("Asia/Seoul");

	/**
	 * 목표 상태(예약 적용/만료)를 즉시 반영
	 * - 로그인/온보딩 조회 등 요청 시점에도 호출
	 */
	@Transactional
	public void applyGoalStateIfNeeded(Member member, LocalDate today) {
		applyPendingIfDue(member, today);
		expireGoalIfNeeded(member, today);
	}

	@Transactional
	// 하루 마감 직후(00:01)에 전일/전주 결과 확정 및 목표 반영
	@Scheduled(cron = "0 1 0 * * *", zone = "Asia/Seoul")
	public void processDueGoals() {
		LocalDate today = LocalDate.now(KST);
		// 전일 일간 목표 결과 확정
		goalResultService.recordDailyResults(today.minusDays(1));
		// 월요일이면 전주(일요일 종료) 주간 목표 결과 확정
		if (today.getDayOfWeek() == DayOfWeek.MONDAY) {
			goalResultService.recordWeeklyResults(today.minusDays(1));
		}
		// 만료 또는 적용 예정 목표 처리
		List<Member> members = memberRepository.findMembersForGoalUpdate(today);
		for (Member member : members) {
			applyGoalStateIfNeeded(member, today);
		}
	}

	/**
	 * 목표 변경 적용일을 계산한다.
	 * - 월요일이면 당일 적용
	 * - 그 외 요일이면 다음 주 월요일 적용
	 */
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

		// 적용일이 되면 예약 목표를 즉시 반영
		GoalRetention pendingRetention = member.getPendingGoalRetention();
		member.applyGoalSetting(member.getPendingDailyGoal(), pendingRetention, pendingApplyDate);
		member.clearPendingGoalSetting();
	}

	private void expireGoalIfNeeded(Member member, LocalDate today) {
		LocalDate endDate = member.getGoalEndDate();
		// 종료일을 지났으면 목표 OFF 처리
		if (endDate != null && today.isAfter(endDate)) {
			member.applyGoalSetting(null, null, today);
		}
	}
}
