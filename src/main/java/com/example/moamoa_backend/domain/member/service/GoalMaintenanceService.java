package com.example.moamoa_backend.domain.member.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.moamoa_backend.domain.member.entity.Member;
import com.example.moamoa_backend.domain.member.enums.GoalRetention;
import com.example.moamoa_backend.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoalMaintenanceService {

	private static final ZoneId KST = ZoneId.of("Asia/Seoul");

	private final MemberRepository memberRepository;
	private final GoalResultService goalResultService;

	/**
	 * 목표 상태(예약 적용/만료)를 즉시 반영.
	 * 로그인/온보딩 조회 등 사용자 요청 시점에도 호출하는 용도.
	 */
	@Transactional
	public void applyGoalStateIfNeeded(Member member, LocalDate today) {
		applyPendingIfDue(member, today);
		expireGoalIfNeeded(member, today);
	}

	/**
	 * 하루 마감 직후(00:01 KST)에 전일/전주 결과 확정 및 목표 상태를 반영.
	 *  전일 DAILY 결과 확정
	 *  월요일이면 전주 종료(일요일) WEEKLY 결과 확정
	 *  예약 적용/만료 대상 멤버를 조회하여 목표 상태 반영
	 *
	 */
	@Transactional
	@Scheduled(cron = "0 1 0 * * *", zone = "Asia/Seoul")
	public void processDueGoals() {
		LocalDate today = LocalDate.now(KST);

		// 1) 전일 DAILY 목표 결과 확정
		goalResultService.recordDailyResults(today.minusDays(1));

		// 2) 월요일이면 전주(일요일 종료) WEEKLY 목표 결과 확정
		if (today.getDayOfWeek() == DayOfWeek.MONDAY) {
			goalResultService.recordWeeklyResults(today.minusDays(1));
		}

		// 3) 예약 적용/만료 목표 반영
		List<Member> members = memberRepository.findMembersForGoalUpdate(today);
		for (Member member : members) {
			applyGoalStateIfNeeded(member, today);
		}
	}

	/**
	 * 목표 변경 적용일(월요일)을 계산.
	 * 정책:
	 *  오늘이 월요일이면 당일 적용
	 *  그 외 요일이면 다음 주 월요일 적용
	 *
	 */
	public LocalDate resolveApplyDate(LocalDate today) {
		if (today.getDayOfWeek() == DayOfWeek.MONDAY) {
			return today;
		}
		return today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
	}

	/**
	 * 예약된 목표 설정(pending)을 적용일에 반영.
	 * 정책:
	 *  pendingApplyDate가 오늘보다 이후면 아직 미적용
	 *  적용일이 되면 "주간 목표"만 계산 (daily는 즉시 적용된 상태)
	 *  pendingDailyGoal이 없으면 pending 상태만 정리
	 *
	 */
	private void applyPendingIfDue(Member member, LocalDate today) {
		LocalDate pendingApplyDate = member.getPendingApplyDate();
		if (pendingApplyDate == null || pendingApplyDate.isAfter(today)) {
			return;
		}

		Integer pendingDailyGoal = member.getPendingDailyGoal();
		if (pendingDailyGoal == null) {
			member.clearPendingGoalSetting();
			return;
		}

		GoalRetention pendingRetention = member.getPendingGoalRetention();
		member.applyWeeklyGoalNow(pendingDailyGoal, pendingRetention, pendingApplyDate);
		member.clearPendingGoalSetting();
	}

	/**
	 * 목표 종료일을 기준으로 목표를 만료 처리.
	 * 정책: 종료일을 지났으면 목표 OFF 처리(일간 목표를 null로 내려 전체 목표 상태를 정리).
	 */
	private void expireGoalIfNeeded(Member member, LocalDate today) {
		LocalDate endDate = member.getGoalEndDate();
		if (endDate != null && today.isAfter(endDate)) {
			member.applyDailyGoalNow(null); // OFF: 전부 정리
		}
	}
}
