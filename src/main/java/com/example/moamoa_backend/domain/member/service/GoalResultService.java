package com.example.moamoa_backend.domain.member.service;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.moamoa_backend.domain.member.dto.res.GoalPopupResponseDto;
import com.example.moamoa_backend.domain.member.entity.GoalResult;
import com.example.moamoa_backend.domain.member.entity.Member;
import com.example.moamoa_backend.domain.member.enums.GoalResultStatus;
import com.example.moamoa_backend.domain.member.enums.GoalResultType;
import com.example.moamoa_backend.domain.member.exception.MemberException;
import com.example.moamoa_backend.domain.member.exception.code.MemberErrorCode;
import com.example.moamoa_backend.domain.member.repository.GoalResultRepository;
import com.example.moamoa_backend.domain.member.repository.MemberRepository;
import com.example.moamoa_backend.domain.wallet.enums.TransactionType;
import com.example.moamoa_backend.domain.wallet.repository.WalletHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoalResultService {

	private final GoalResultRepository goalResultRepository;
	private final MemberRepository memberRepository;
	private final WalletHistoryRepository walletHistoryRepository;
	private static final ZoneId KST = ZoneId.of("Asia/Seoul");
	private static final int IN_CLAUSE_BATCH_SIZE = 1000; // (대량 회원 대비)

	/**
	 *   “목표 실패 팝업” 조회 API용
	 * - 홈 진입 시점에 호출하면 됨
	 * - 기본 규칙:
	 *   - DAILY: 어제 결과(실패)를 아직 안 봤으면 popups에 포함
	 *   - WEEKLY: 지난 주 일요일(주차 종료) 결과를 아직 안 봤으면 popups에 포함
	 * - 없으면 가능한 경우 생성해서 포함
	 */
	@Transactional
	public GoalPopupResponseDto getGoalPopups(Long memberId) {
		LocalDate today = LocalDate.now(KST);
		LocalDate yesterday = today.minusDays(1);

		LocalDate lastWeekEnd = today.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY)); // 지난 주 일요일

		List<GoalPopupResponseDto.Popup> popups = new ArrayList<>();

		// 1) 일간(어제)
		GoalResult daily = goalResultRepository
			.findByMemberIdAndGoalTypeAndGoalDate(memberId, GoalResultType.DAILY, yesterday)
			.or(() -> createDailyGoalResultIfPossible(memberId, yesterday))
			.orElse(null);

		//fail만 노출
		if (daily != null
			&& daily.getPopupShownAt() == null
			&& daily.getStatus() == GoalResultStatus.FAIL) {
			popups.add(GoalPopupResponseDto.Popup.from(daily));
		}

		// 2) 주간(지난 주)
		GoalResult weekly = goalResultRepository
			.findByMemberIdAndGoalTypeAndGoalDate(memberId, GoalResultType.WEEKLY, lastWeekEnd)
			.or(() -> createWeeklyGoalResultIfPossible(memberId, lastWeekEnd))
			.orElse(null);

		//fail만 노출
		if (weekly != null
			&& weekly.getPopupShownAt() == null
			&& weekly.getStatus() == GoalResultStatus.FAIL) {
			popups.add(GoalPopupResponseDto.Popup.from(weekly));
		}

		return new GoalPopupResponseDto(popups);
	}

	/**
	 *   goalResultId 기준으로 팝업 봤음 처리
	 * - 일간/주간 하나로 처리
	 */
	@Transactional
	public void markPopupShown(Long memberId, Long goalResultId) {
		GoalResult gr = goalResultRepository.findByIdAndMemberId(goalResultId, memberId)
			.orElseThrow(() -> new MemberException(MemberErrorCode.GOAL_RESULT_NOT_FOUND));

		gr.markPopupShown(LocalDateTime.now(KST));
	}

	/**
	 * 전일 일간 목표 결과를 일괄 확정한다. (스케줄러용)
	 *  - 기존: member마다 exists 조회 → N+1
	 * 	- 개선: 해당 날짜/타입 결과를 한 번에 조회해 Set으로 비교
	 */
	@Transactional
	public void recordDailyResults(LocalDate goalDate) {
		List<Member> members = memberRepository.findMembersWithDailyGoal();
		if (members.isEmpty())
			return;

		List<Long> memberIds = members.stream()
			.map(Member::getId)
			.collect(Collectors.toList());

		Set<Long> existingMemberIds = new HashSet<>();
		for (List<Long> chunk : partition(memberIds, IN_CLAUSE_BATCH_SIZE)) {
			existingMemberIds.addAll(
				goalResultRepository.findExistingMemberIdsByGoalTypeAndGoalDate(
					GoalResultType.DAILY,
					goalDate,
					chunk
				)
			);
		}

		for (Member member : members) {

			if (existingMemberIds.contains(member.getId()))
				continue; //  (exists 쿼리 제거)
			createDailyGoalResult(member, goalDate);

		}
	}

	/**
	 * 주간 목표 결과를 일괄 확정한다. (스케줄러용)
	 * - 기존: member마다 exists 조회 → N+1
	 * - 개선: 해당 날짜/타입 결과를 한 번에 조회해 Set으로 비교
	 */
	@Transactional
	public void recordWeeklyResults(LocalDate weekEndDate) {
		weekEndDate = resolveWeekEnd(weekEndDate);

		List<Member> members = memberRepository.findMembersWithWeeklyGoal();
		if (members.isEmpty())
			return;

		List<Long> memberIds = members.stream()
			.map(Member::getId)
			.collect(Collectors.toList());

		Set<Long> existingMemberIds = new HashSet<>();
		for (List<Long> chunk : partition(memberIds, IN_CLAUSE_BATCH_SIZE)) {
			existingMemberIds.addAll(
				goalResultRepository.findExistingMemberIdsByGoalTypeAndGoalDate(
					GoalResultType.WEEKLY,
					weekEndDate,
					chunk
				)
			);
		}

		for (Member member : members) {
			if (existingMemberIds.contains(member.getId()))
				continue; //  (exists 쿼리 제거)

			createWeeklyGoalResult(member, weekEndDate);

		}
	}

	private Optional<GoalResult> createDailyGoalResultIfPossible(Long memberId, LocalDate goalDate) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

		if (member.getDailyGoal() == null)
			return Optional.empty();

		// 오늘/미래 날짜는 FAIL 확정 금지 → 어제까지만 생성 허용
		LocalDate yesterday = LocalDate.now(KST).minusDays(1);
		if (goalDate.isAfter(yesterday))
			return Optional.empty();

		int target = member.getDailyGoal();
		int achieved = countMissionCompletions(memberId, goalDate.atStartOfDay(), goalDate.plusDays(1).atStartOfDay());
		GoalResultStatus status = achieved >= target ? GoalResultStatus.SUCCESS : GoalResultStatus.FAIL;
		//  성공이면 팝업도 안 쓰고, 레코드 생성도 안 함
		if (status == GoalResultStatus.SUCCESS)
			return Optional.empty();

		try {
			return Optional.of(goalResultRepository.save(
				GoalResult.createDaily(member, goalDate, target, achieved, status)
			));
		} catch (DataIntegrityViolationException e) {
			return goalResultRepository.findByMemberIdAndGoalTypeAndGoalDate(memberId, GoalResultType.DAILY, goalDate);
		}
	}

	private Optional<GoalResult> createWeeklyGoalResultIfPossible(Long memberId, LocalDate weekEndDate) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

		if (member.getWeeklyGoal() == null)
			return Optional.empty();

		// 이번 주는 아직 끝나지 않았으니 FAIL 확정 금지 → 지난 주 일요일까지만 생성 허용
		LocalDate lastSunday = LocalDate.now(KST).with(TemporalAdjusters.previous(DayOfWeek.SUNDAY));
		if (weekEndDate.isAfter(lastSunday))
			return Optional.empty();

		int target = member.getWeeklyGoal();
		LocalDate weekStart = weekEndDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

		int achieved = countMissionCompletions(memberId, weekStart.atStartOfDay(),
			weekEndDate.plusDays(1).atStartOfDay());
		GoalResultStatus status = achieved >= target ? GoalResultStatus.SUCCESS : GoalResultStatus.FAIL;
		//  성공이면 팝업도 안 쓰고, 레코드 생성도 안 함
		if (status == GoalResultStatus.SUCCESS)
			return Optional.empty();

		try {
			return Optional.of(goalResultRepository.save(
				GoalResult.createWeekly(member, weekEndDate, target, achieved, status)
			));
		} catch (DataIntegrityViolationException e) {
			return goalResultRepository.findByMemberIdAndGoalTypeAndGoalDate(memberId, GoalResultType.WEEKLY,
				weekEndDate);
		}
	}

	private GoalResult createDailyGoalResult(Member member, LocalDate goalDate) {
		int targetCount = member.getDailyGoal();
		int achievedCount = countMissionCompletions(
			member.getId(),
			goalDate.atStartOfDay(),
			goalDate.plusDays(1).atStartOfDay()
		);
		GoalResultStatus status = achievedCount >= targetCount ? GoalResultStatus.SUCCESS : GoalResultStatus.FAIL;

		try { //
			return goalResultRepository.save(
				GoalResult.createDaily(member, goalDate, targetCount, achievedCount, status)
			);
		} catch (DataIntegrityViolationException e) { // 동시성/중복 insert 방어
			return goalResultRepository.findByMemberIdAndGoalTypeAndGoalDate(member.getId(), GoalResultType.DAILY,
					goalDate)
				.orElseThrow(() -> new MemberException(MemberErrorCode.GOAL_RESULT_NOT_FOUND));
		}
	}

	private GoalResult createWeeklyGoalResult(Member member, LocalDate weekEndDate) {
		int targetCount = member.getWeeklyGoal();
		LocalDate weekStart = weekEndDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		int achievedCount = countMissionCompletions(
			member.getId(),
			weekStart.atStartOfDay(),
			weekEndDate.plusDays(1).atStartOfDay()
		);
		GoalResultStatus status = achievedCount >= targetCount ? GoalResultStatus.SUCCESS : GoalResultStatus.FAIL;
		try {
			return goalResultRepository.save(
				GoalResult.createWeekly(member, weekEndDate, targetCount, achievedCount, status)
			);
		} catch (DataIntegrityViolationException e) { // 동시성/중복 insert 방어
			return goalResultRepository.findByMemberIdAndGoalTypeAndGoalDate(member.getId(), GoalResultType.WEEKLY,
					weekEndDate)
				.orElseThrow(() -> new MemberException(MemberErrorCode.GOAL_RESULT_NOT_FOUND));
		}
	}

	private int countMissionCompletions(Long memberId, LocalDateTime startAt, LocalDateTime endAt) {
		return Math.toIntExact(
			walletHistoryRepository.countByMemberAndTypeBetween(
				memberId,
				TransactionType.MISSION_COMPLETE,
				startAt,
				endAt
			)
		);
	}

	private LocalDate resolveWeekEnd(LocalDate dateInWeek) {
		return dateInWeek.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
	}

	// IN 절 파라미터 너무 커지는 것 방지용 유틸
	private <T> List<List<T>> partition(List<T> list, int size) {
		List<List<T>> result = new ArrayList<>();
		for (int i = 0; i < list.size(); i += size) {
			result.add(list.subList(i, Math.min(i + size, list.size())));
		}
		return result;
	}
}
