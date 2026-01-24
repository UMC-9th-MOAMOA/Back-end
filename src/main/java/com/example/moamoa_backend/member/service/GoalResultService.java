package com.example.moamoa_backend.member.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.moamoa_backend.member.dto.GoalResultResponseDto;
import com.example.moamoa_backend.member.entity.GoalResult;
import com.example.moamoa_backend.member.entity.Member;
import com.example.moamoa_backend.member.enums.GoalResultStatus;
import com.example.moamoa_backend.member.enums.GoalResultType;
import com.example.moamoa_backend.member.exception.MemberException;
import com.example.moamoa_backend.member.exception.code.MemberErrorCode;
import com.example.moamoa_backend.member.repository.GoalResultRepository;
import com.example.moamoa_backend.member.repository.MemberRepository;
import com.example.moamoa_backend.wallet.enums.TransactionType;
import com.example.moamoa_backend.wallet.repository.WalletHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoalResultService {

	private final GoalResultRepository goalResultRepository;
	private final MemberRepository memberRepository;
	private final WalletHistoryRepository walletHistoryRepository;

	/**
	 * 특정 날짜의 일간 목표 결과를 조회한다.
	 * - 결과가 없으면 가능한 경우 즉시 생성한다.
	 */
	@Transactional
	public Optional<GoalResultResponseDto> getDailyGoalResult(Long memberId, LocalDate goalDate) {
		return goalResultRepository
			.findByMemberIdAndGoalTypeAndGoalDate(memberId, GoalResultType.DAILY, goalDate)
			.map(GoalResultResponseDto::from)
			.or(() -> createDailyGoalResultIfPossible(memberId, goalDate));
	}

	/**
	 * 특정 주차의 주간 목표 결과를 조회한다.
	 * - dateInWeek 기준으로 해당 주의 일요일(주차 종료일) 결과를 반환한다.
	 * - 결과가 없으면 가능한 경우 즉시 생성한다.
	 */
	@Transactional
	public Optional<GoalResultResponseDto> getWeeklyGoalResult(Long memberId, LocalDate dateInWeek) {
		LocalDate weekEnd = resolveWeekEnd(dateInWeek);
		return goalResultRepository
			.findByMemberIdAndGoalTypeAndGoalDate(memberId, GoalResultType.WEEKLY, weekEnd)
			.map(GoalResultResponseDto::from)
			.or(() -> createWeeklyGoalResultIfPossible(memberId, weekEnd));
	}

	/**
	 * 전일 일간 목표 결과를 일괄 확정한다.
	 * - 이미 존재하면 재생성하지 않는다.
	 */
	@Transactional
	public void recordDailyResults(LocalDate goalDate) {
		List<Member> members = memberRepository.findMembersWithDailyGoal();
		for (Member member : members) {
			if (goalResultRepository.findByMemberIdAndGoalTypeAndGoalDate(
				member.getId(),
				GoalResultType.DAILY,
				goalDate
			).isPresent()) {
				continue;
			}
			createDailyGoalResult(member, goalDate);
		}
	}

	/**
	 * 주간 목표 결과를 일괄 확정한다.
	 * - 이미 존재하면 재생성하지 않는다.
	 */
	@Transactional
	public void recordWeeklyResults(LocalDate weekEndDate) {
		List<Member> members = memberRepository.findMembersWithWeeklyGoal();
		for (Member member : members) {
			if (goalResultRepository.findByMemberIdAndGoalTypeAndGoalDate(
				member.getId(),
				GoalResultType.WEEKLY,
				weekEndDate
			).isPresent()) {
				continue;
			}
			createWeeklyGoalResult(member, weekEndDate);
		}
	}

	private Optional<GoalResultResponseDto> createDailyGoalResultIfPossible(Long memberId, LocalDate goalDate) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
		// 목표 OFF 상태면 결과를 만들지 않는다.
		if (member.getDailyGoal() == null) {
			return Optional.empty();
		}
		GoalResult goalResult = createDailyGoalResult(member, goalDate);
		return Optional.of(GoalResultResponseDto.from(goalResult));
	}

	private Optional<GoalResultResponseDto> createWeeklyGoalResultIfPossible(Long memberId, LocalDate weekEndDate) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
		// 목표 OFF 상태면 결과를 만들지 않는다.
		if (member.getWeeklyGoal() == null) {
			return Optional.empty();
		}
		GoalResult goalResult = createWeeklyGoalResult(member, weekEndDate);
		return Optional.of(GoalResultResponseDto.from(goalResult));
	}

	private GoalResult createDailyGoalResult(Member member, LocalDate goalDate) {
		int targetCount = member.getDailyGoal();
		// 지정일의 미션 보상(완료) 횟수를 집계
		int achievedCount = countMissionRewards(member.getId(), goalDate);
		GoalResultStatus status = achievedCount >= targetCount ? GoalResultStatus.SUCCESS : GoalResultStatus.FAIL;
		GoalResult goalResult = GoalResult.createDaily(member, goalDate, targetCount, achievedCount, status);
		return goalResultRepository.save(goalResult);
	}

	private GoalResult createWeeklyGoalResult(Member member, LocalDate weekEndDate) {
		int targetCount = member.getWeeklyGoal();
		LocalDate weekStart = weekEndDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		// 주차 범위 내 미션 보상(완료) 횟수를 집계
		int achievedCount = countMissionRewardsBetween(member.getId(), weekStart, weekEndDate.plusDays(1));
		GoalResultStatus status = achievedCount >= targetCount ? GoalResultStatus.SUCCESS : GoalResultStatus.FAIL;
		GoalResult goalResult = GoalResult.createWeekly(member, weekEndDate, targetCount, achievedCount, status);
		return goalResultRepository.save(goalResult);
	}

	private int countMissionRewards(Long memberId, LocalDate goalDate) {
		return countMissionRewardsBetween(memberId, goalDate, goalDate.plusDays(1));
	}

	private int countMissionRewardsBetween(Long memberId, LocalDate startDate, LocalDate endDate) {
		LocalDateTime startAt = startDate.atStartOfDay();
		LocalDateTime endAt = endDate.atStartOfDay();
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
		// 월~일 기준에서 주차 종료일(일요일)로 정규화
		return dateInWeek.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
	}
}
