package com.example.moamoa_backend.domain.member.scheduler;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.moamoa_backend.domain.member.entity.Member;
import com.example.moamoa_backend.domain.member.repository.MemberRepository;
import com.example.moamoa_backend.domain.member.service.GoalMaintenanceService;
import com.example.moamoa_backend.domain.member.service.GoalResultService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberProcessGoalScheduler {

	private static final ZoneId KST = ZoneId.of("Asia/Seoul");

	private final MemberRepository memberRepository;
	private final GoalResultService goalResultService;
	private final GoalMaintenanceService goalMaintenanceService;

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
			goalMaintenanceService.applyGoalStateIfNeeded(member, today);
		}
	}
}
