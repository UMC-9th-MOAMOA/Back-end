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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalResultService {

	private static final ZoneId KST = ZoneId.of("Asia/Seoul");
	private static final int IN_CLAUSE_BATCH_SIZE = 1000; // IN 절 파라미터 과대 방지 (대량 회원 대비)

	private final GoalResultRepository goalResultRepository;
	private final MemberRepository memberRepository;
	private final WalletHistoryRepository walletHistoryRepository;

	/**
	 * 목표 실패 팝업 조회.
	 * 홈 진입 시점에 호출하는 용도.
	 *   DAILY: 어제 결과가 FAIL이고 아직 미노출(popupShownAt == null)이면 포함
	 *   WEEKLY: 지난 주 종료일(지난 주 일요일) 결과가 FAIL이고 아직 미노출이면 포함
	 * 해당 날짜의 결과 레코드가 없으면, 생성 가능한 조건에서만 FAIL 레코드를 생성해 포함.
	 * (SUCCESS는 미션 답안 제출 시에 생성. 여기에선 팝업 생성하지 않음.)
	 */
	@Transactional
	public GoalPopupResponseDto getGoalPopups(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

		LocalDate joinedDate = member.getCreatedAt().toLocalDate(); // BaseEntity createdAt 가정
		LocalDate joinedWeekEnd = joinedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

		LocalDate today = LocalDate.now(KST);
		LocalDate yesterday = today.minusDays(1);

		LocalDate lastWeekEnd = today.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY)); // 지난 주 일요일(주차 종료일)

		List<GoalPopupResponseDto.Popup> popups = new ArrayList<>();

		// 1) DAILY(어제): "가입일(포함) 이전"이면 절대 팝업/생성 안 함
		if (yesterday.isAfter(joinedDate)) {
			GoalResult daily = goalResultRepository
				.findByMemberIdAndGoalTypeAndGoalDate(memberId, GoalResultType.DAILY, yesterday)
				.or(() -> createDailyGoalResultIfPossible(member, yesterday))
				.orElse(null);

			if (daily != null
				&& daily.getPopupShownAt() == null
				&& daily.getStatus() == GoalResultStatus.FAIL) {
				popups.add(GoalPopupResponseDto.Popup.from(daily));
			}
		}

		// 2) WEEKLY(지난 주 종료일): "가입한 주의 종료일(포함) 이전"이면 팝업/생성 안 함
		if (lastWeekEnd.isAfter(joinedWeekEnd)) {
			GoalResult weekly = goalResultRepository
				.findByMemberIdAndGoalTypeAndGoalDate(memberId, GoalResultType.WEEKLY, lastWeekEnd)
				.or(() -> createWeeklyGoalResultIfPossible(member, lastWeekEnd))
				.orElse(null);

			if (weekly != null
				&& weekly.getPopupShownAt() == null
				&& weekly.getStatus() == GoalResultStatus.FAIL) {
				popups.add(GoalPopupResponseDto.Popup.from(weekly));
			}
		}

		return new GoalPopupResponseDto(popups);
	}

	/**
	 * 팝업 "확인" 처리.
	 * GoalResult 단위로 처리하며, DAILY/WEEKLY 구분 없이 동일 로직을 사용한다.
	 */
	@Transactional
	public void markPopupShown(Long memberId, Long goalResultId) {
		GoalResult gr = goalResultRepository.findByIdAndMemberId(goalResultId, memberId)
			.orElseThrow(() -> new MemberException(MemberErrorCode.GOAL_RESULT_NOT_FOUND));

		gr.markPopupShown(LocalDateTime.now(KST));
	}

	/**
	 * 일간 목표 결과를 일괄 확정한다. (스케줄러용)
	 * N+1 exists 조회를 피하기 위해, 해당 날짜/타입 결과를 선조회하여 Set으로 비교한다.
	 *
	 * 정책:
	 *   가입일(포함) 이전 날짜(goalDate <= joinedDate)는 생성 스킵 (첫 가입 팝업/데이터 오염 방지)
	 */
	@Transactional
	public void recordDailyResults(LocalDate goalDate) {
		List<Member> members = memberRepository.findMembersWithDailyGoal();
		if (members.isEmpty()) {
			return;
		}

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
			// 가입일(포함) 이전 날짜는 결과 생성 스킵
			LocalDate joinedDate = member.getCreatedAt().toLocalDate();
			if (!goalDate.isAfter(joinedDate)) { // goalDate <= joinedDate
				continue;
			}

			if (existingMemberIds.contains(member.getId())) {
				continue; // 이미 존재하면 skip (exists 쿼리 제거)
			}
			try {
				createDailyGoalResult(member, goalDate);
			} catch (Exception e) {
				log.error("DAILY GoalResult 생성 실패 memberId={} goalDate={} dailyGoal={}",
					member.getId(), goalDate, member.getDailyGoal(), e);
			}
		}
	}

	/**
	 * 주간 목표 결과를 일괄 확정한다. (스케줄러용)
	 *
	 * 입력 날짜가 주중일 수 있으므로, 주차 종료일(SUNDAY)로 정규화하여 처리한다.
	 * N+1 exists 조회를 피하기 위해, 해당 날짜/타입 결과를 선조회하여 Set으로 비교한다.
	 *
	 * 정책:
	 *   가입한 주의 종료일(포함) 이전(weekEndDate <= joinedWeekEnd)은 생성 스킵 (첫 가입 팝업/데이터 오염 방지)
	 */
	@Transactional
	public void recordWeeklyResults(LocalDate weekEndDate) {
		weekEndDate = resolveWeekEnd(weekEndDate);

		List<Member> members = memberRepository.findMembersWithWeeklyGoal();
		if (members.isEmpty()) {
			return;
		}

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
			// 가입한 주의 종료일(포함) 이전은 결과 생성 스킵
			LocalDate joinedDate = member.getCreatedAt().toLocalDate();
			LocalDate joinedWeekEnd = joinedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
			if (!weekEndDate.isAfter(joinedWeekEnd)) { // weekEndDate <= joinedWeekEnd
				continue;
			}

			if (existingMemberIds.contains(member.getId())) {
				continue; // 이미 존재하면 skip (exists 쿼리 제거)
			}
			try {
				createWeeklyGoalResult(member, weekEndDate);
			} catch (Exception e) {
				log.error("WEEKLY GoalResult 생성 실패 memberId={} weekEndDate={} weeklyGoal={}",
					member.getId(), weekEndDate, member.getWeeklyGoal(), e);
			}
		}
	}

	/**
	 * (팝업 조회 경로) 일간 GoalResult가 없을 때, 생성 가능한 조건에서만 FAIL 레코드를 생성한다.
	 *
	 * 정책:
	 *   가입일(포함) 이전 날짜(goalDate <= joinedDate)는 생성 금지 (첫 가입 팝업/데이터 오염 방지)
	 *   member.dailyGoal이 없으면 생성하지 않는다.
	 *   오늘/미래 날짜는 FAIL 확정 금지 → 어제까지 허용.
	 *   SUCCESS는 팝업 대상이 아니므로 생성하지 않는다.
	 *   동시성/중복 insert는 unique 제약 + 재조회로 방어한다.
	 */
	private Optional<GoalResult> createDailyGoalResultIfPossible(Member member, LocalDate goalDate) {
		Long memberId = member.getId();

		// 가입일(포함) 이전은 생성 금지
		LocalDate joinedDate = member.getCreatedAt().toLocalDate();
		if (!goalDate.isAfter(joinedDate)) { // goalDate <= joinedDate
			return Optional.empty();
		}

		if (member.getDailyGoal() == null || member.getDailyGoal() <= 0) {
			return Optional.empty();
		}

		// 오늘/미래 날짜는 FAIL 확정 금지 → 어제까지만 생성 허용
		LocalDate yesterday = LocalDate.now(KST).minusDays(1);
		if (goalDate.isAfter(yesterday)) {
			return Optional.empty();
		}

		int target = member.getDailyGoal();
		int achieved = countMissionCompletions(
			memberId,
			goalDate.atStartOfDay(),
			goalDate.plusDays(1).atStartOfDay()
		);
		GoalResultStatus status = achieved >= target ? GoalResultStatus.SUCCESS : GoalResultStatus.FAIL;

		// SUCCESS는 팝업 대상이 아니므로 레코드 생성도 하지 않음
		if (status == GoalResultStatus.SUCCESS) {
			return Optional.empty();
		}

		try {
			return Optional.of(goalResultRepository.save(
				GoalResult.createDaily(member, goalDate, target, achieved, status)
			));
		} catch (DataIntegrityViolationException e) {
			// 동시성/중복 insert 방어: 이미 생성된 경우 재조회
			return goalResultRepository.findByMemberIdAndGoalTypeAndGoalDate(
				memberId,
				GoalResultType.DAILY,
				goalDate
			);
		}
	}

	/**
	 * (팝업 조회 경로) 주간 GoalResult가 없을 때, 생성 가능한 조건에서만 FAIL 레코드를 생성한다.
	 *
	 * 정책:
	 *   가입한 주의 종료일(포함) 이전(weekEndDate <= joinedWeekEnd)은 생성 금지 (첫 가입 팝업/데이터 오염 방지)
	 *   member.weeklyGoal이 없으면 생성하지 않는다.
	 *   이번 주는 아직 종료되지 않았으므로 FAIL 확정 금지 → 지난 주 종료일(지난 주 일요일)까지만 허용.
	 *   SUCCESS는 팝업 대상이 아니므로 생성하지 않는다.
	 *   동시성/중복 insert는 unique 제약 + 재조회로 방어한다.
	 */
	private Optional<GoalResult> createWeeklyGoalResultIfPossible(Member member, LocalDate weekEndDate) {
		Long memberId = member.getId();
		weekEndDate = resolveWeekEnd(weekEndDate);

		// 가입한 주의 종료일(포함) 이전은 생성 금지
		LocalDate joinedDate = member.getCreatedAt().toLocalDate();
		LocalDate joinedWeekEnd = joinedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
		if (!weekEndDate.isAfter(joinedWeekEnd)) { // weekEndDate <= joinedWeekEnd
			return Optional.empty();
		}

		if (member.getWeeklyGoal() == null || member.getWeeklyGoal() <= 0) {
			return Optional.empty();
		}

		// 이번 주는 아직 끝나지 않았으니 FAIL 확정 금지 → 지난 주 일요일까지만 생성 허용
		LocalDate lastSunday = LocalDate.now(KST).with(TemporalAdjusters.previous(DayOfWeek.SUNDAY));
		if (weekEndDate.isAfter(lastSunday)) {
			return Optional.empty();
		}

		int target = member.getWeeklyGoal();
		LocalDate weekStart = weekEndDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

		int achieved = countMissionCompletions(
			memberId,
			weekStart.atStartOfDay(),
			weekEndDate.plusDays(1).atStartOfDay()
		);
		GoalResultStatus status = achieved >= target ? GoalResultStatus.SUCCESS : GoalResultStatus.FAIL;

		// SUCCESS는 팝업 대상이 아니므로 레코드 생성도 하지 않음
		if (status == GoalResultStatus.SUCCESS) {
			return Optional.empty();
		}

		try {
			return Optional.of(goalResultRepository.save(
				GoalResult.createWeekly(member, weekEndDate, target, achieved, status)
			));
		} catch (DataIntegrityViolationException e) {
			// 동시성/중복 insert 방어: 이미 생성된 경우 재조회
			return goalResultRepository.findByMemberIdAndGoalTypeAndGoalDate(
				memberId,
				GoalResultType.WEEKLY,
				weekEndDate
			);
		}
	}

	/**
	 * (스케줄러 경로) 일간 GoalResult를 생성한다.
	 * 동시성/중복 insert는 unique 제약 + 재조회로 방어한다.
	 */
	private GoalResult createDailyGoalResult(Member member, LocalDate goalDate) {
		int targetCount = member.getDailyGoal();
		int achievedCount = countMissionCompletions(
			member.getId(),
			goalDate.atStartOfDay(),
			goalDate.plusDays(1).atStartOfDay()
		);
		GoalResultStatus status = achievedCount >= targetCount ? GoalResultStatus.SUCCESS : GoalResultStatus.FAIL;

		try {
			return goalResultRepository.save(
				GoalResult.createDaily(member, goalDate, targetCount, achievedCount, status)
			);
		} catch (DataIntegrityViolationException e) {
			// 동시성/중복 insert 방어: 이미 생성된 경우 재조회
			return goalResultRepository.findByMemberIdAndGoalTypeAndGoalDate(
					member.getId(),
					GoalResultType.DAILY,
					goalDate
				)
				.orElseThrow(() -> new MemberException(MemberErrorCode.GOAL_RESULT_NOT_FOUND));
		}
	}

	/**
	 * (스케줄러 경로) 주간 GoalResult를 생성한다.
	 * 동시성/중복 insert는 unique 제약 + 재조회로 방어.
	 */
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
		} catch (DataIntegrityViolationException e) {
			// 동시성/중복 insert 방어: 이미 생성된 경우 재조회
			return goalResultRepository.findByMemberIdAndGoalTypeAndGoalDate(
					member.getId(),
					GoalResultType.WEEKLY,
					weekEndDate
				)
				.orElseThrow(() -> new MemberException(MemberErrorCode.GOAL_RESULT_NOT_FOUND));
		}
	}

	// ================== 헬퍼 메서드 ==================

	/**
	 * 기간(startAt inclusive, endAt exclusive) 내 미션 완료 횟수를 집계.
	 */
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

	/**
	 * 주중 임의 날짜를 주차 종료일(SUNDAY)로 정규화.
	 */
	private LocalDate resolveWeekEnd(LocalDate dateInWeek) {
		return dateInWeek.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
	}

	/**
	 * IN 절 파라미터 과다(예: 1000 초과) 방지를 위한 리스트 분할 유틸.
	 */
	private <T> List<List<T>> partition(List<T> list, int size) {
		List<List<T>> result = new ArrayList<>();
		for (int i = 0; i < list.size(); i += size) {
			result.add(list.subList(i, Math.min(i + size, list.size())));
		}
		return result;
	}
}
