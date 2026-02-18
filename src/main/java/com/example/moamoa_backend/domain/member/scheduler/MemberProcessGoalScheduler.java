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
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
	@Scheduled(cron = "0 1 0 * * *", zone = "Asia/Seoul")
	public void processDueGoals() {
		LocalDate today = LocalDate.now(KST);

		// 1) 전일 DAILY 목표 결과 확정
		LocalDate dailyDate = today.minusDays(1);
		try {
			goalResultService.recordDailyResults(dailyDate);
		} catch (Exception e) {
			log.error("DAILY 결과 확정 실패 goalDate={}", dailyDate, e);
		}

		// 2) 월요일이면 전주(일요일 종료) WEEKLY 목표 결과 확정

		if (today.getDayOfWeek() == DayOfWeek.MONDAY) {
			LocalDate baseDate = today.minusDays(1); // 일요일
			try {
				goalResultService.recordWeeklyResults(baseDate);
			} catch (Exception e) {
				log.error("WEEKLY 결과 확정 실패 baseDate={}", baseDate, e);
			}
		}

		// 3) 예약 적용/만료 목표 반영
		List<Long> memberIds = memberRepository.findMemberIdsForGoalUpdate(today);
		for (Long memberId : memberIds) {
			try {
				goalMaintenanceService.applyGoalStateIfNeeded(memberId, today);
			} catch (Exception e) {
				log.error("목표 상태 반영 실패 memberId={}", memberId, e);
			}
		}
	}
}
