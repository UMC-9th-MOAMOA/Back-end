package com.example.moamoa_backend.member.service;

import com.example.moamoa_backend.interest.exception.InterestException;
import com.example.moamoa_backend.interest.exception.code.InterestErrorCode;
import com.example.moamoa_backend.interest.repository.SubInterestRepository;
import com.example.moamoa_backend.member.dto.req.OnboardingPatchRequestDto;
import com.example.moamoa_backend.member.dto.res.OnboardingResponseDto;
import com.example.moamoa_backend.member.entity.Member;
import com.example.moamoa_backend.member.entity.mapping.MemberSubInterest;
import com.example.moamoa_backend.member.enums.GoalRetention;
import com.example.moamoa_backend.member.enums.OnboardingUpdateScope;
import com.example.moamoa_backend.member.exception.MemberException;
import com.example.moamoa_backend.member.exception.code.MemberErrorCode;
import com.example.moamoa_backend.member.repository.MemberRepository;
import com.example.moamoa_backend.member.repository.MemberSubInterestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OnboardingService {

	private final MemberRepository memberRepository;
	private final MemberSubInterestRepository memberSubInterestRepository;
	private final SubInterestRepository subInterestRepository;
	private final GoalMaintenanceService goalMaintenanceService;
	private static final ZoneId KST = ZoneId.of("Asia/Seoul");


	/**
	 * 온보딩 수정 API
	 * - 엔드포인트는 하나로 유지하고, scope로 "무엇을 수정할지"만 분기한다.
	 *
	 * scope 정책:
	 * - ALL: selections(필수) + dailyMissionGoal(선택/null 허용)
	 * - INTERESTS: selections(필수)
	 * - GOAL: dailyMissionGoal(필수, 0~5)
	 *
	 * 응답은 항상 최신 상태(ALL)로 내려줘서 프론트 동기화에 유리하게 구성
	 */
	@Transactional
	public OnboardingResponseDto patchOnboarding(Long memberId, OnboardingUpdateScope scope, OnboardingPatchRequestDto req) {

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
		LocalDate today = LocalDate.now(KST);
		// 요청 시점에 예약 적용/만료를 우선 반영
		goalMaintenanceService.applyGoalStateIfNeeded(member, today);


		return switch (scope) {
			case ALL -> {
				requireSelections(req.selections());              // 관심사 최소 1개 이상 필수
				validateGoalRangeIfPresent(req.dailyMissionGoal()); // goal은 null 허용, 값이 있으면 범위만 검증
				validateGoalRetentionIfPresent(req.dailyMissionGoal(), req.goalRetention());

				// "나중에 설정": goal 관련 값이 아예 없으면 목표를 건드리지 않는다.
				if (!(req.dailyMissionGoal() == null && req.goalRetention() == null)) {
					updateGoalSetting(member, req.dailyMissionGoal(), req.goalRetention(), today);
				}

				updateMemberInterestsSmartSync(member, req.selections()); // 관심사 Smart Sync 반영

				// 최신 ALL 상태로 응답 (member 재조회 없이)
				yield toOnboardingResponse(loadSelections(memberId), member);
			}
			case INTERESTS -> {
				requireSelections(req.selections());
				updateMemberInterestsSmartSync(member, req.selections());
				yield toOnboardingResponse(loadSelections(memberId), member);
			}
			case GOAL -> {
				// 설정 화면 토글 OFF 지원: dailyMissionGoal == null 이면 OFF 처리
				if (req.dailyMissionGoal() == null) {
					validateGoalRetentionIfPresent(null, req.goalRetention());
					updateGoalSetting(member, null, null, today);
					yield toOnboardingResponse(loadSelections(memberId), member);
				}

				// 토글 ON 상태에서 값 변경/저장하는 케이스
				validateGoalRange(req.dailyMissionGoal());   // 0~5 범위 검증
				requireGoalRetention(req.goalRetention());

				// 목표 설정(즉시 적용 or 다음 주 예약)
				updateGoalSetting(member, req.dailyMissionGoal(), req.goalRetention(), today);
				yield toOnboardingResponse(loadSelections(memberId), member);
			}
		};
	}

	/**
	 * 온보딩 조회 API
	 * - scope에 따라 필요한 데이터만 내려줄 수 있도록 분기
	 * - OnboardingResponseDto는 NON_NULL 설정이므로 null 필드는 응답에서 빠진다.
	 */
	@Transactional
	public OnboardingResponseDto getMyOnboarding(Long memberId, OnboardingUpdateScope scope) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
		// 조회 시점에도 목표 상태를 최신으로 맞춘다.
		goalMaintenanceService.applyGoalStateIfNeeded(member, LocalDate.now(KST));

		return switch (scope) {
			case ALL -> toOnboardingResponse(loadSelections(memberId), member);
			case INTERESTS -> OnboardingResponseDto.of(loadSelections(memberId), null, null, null, null, null, null);
			case GOAL -> toOnboardingResponse(null, member);
		};
	}

	// ---------------------------
	// Validation (scope별 필수값 체크)
	// ---------------------------

	private void requireSelections(List<OnboardingPatchRequestDto.Selection> selections) {
		if (selections == null || selections.isEmpty()) {
			throw new InterestException(InterestErrorCode.ONBOARDING_SELECTION_REQUIRED);
		}
	}

	private void requireGoal(Integer goal) {
		if (goal == null) {
			throw new InterestException(InterestErrorCode.ONBOARDING_GOAL_REQUIRED);
		}
	}

	private void requireGoalRetention(GoalRetention retention) {
		if (retention == null) {
			throw new InterestException(InterestErrorCode.ONBOARDING_GOAL_RETENTION_REQUIRED);
		}
	}

	private void validateGoalRange(Integer goal) {
		if (goal < 0 || goal > 5) {
			throw new InterestException(InterestErrorCode.ONBOARDING_GOAL_OUT_OF_RANGE);
		}
	}

	/**
	 * ALL scope에서는 goal이 null일 수 있으므로, 값이 있을 때만 범위 검증
	 */
	private void validateGoalRangeIfPresent(Integer goal) {
		if (goal != null) validateGoalRange(goal);
	}


	private void validateGoalRetentionIfPresent(Integer goal, GoalRetention retention) {
		if (goal == null && retention != null) {
			throw new InterestException(InterestErrorCode.ONBOARDING_GOAL_RETENTION_INVALID);
		}
	}

	/**
	 * 목표 설정/변경 로직.
	 * - dailyGoal이 null이면 목표 OFF 처리
	 * - 월요일이면 즉시 적용, 그 외는 다음 주 월요일 예약
	 */
	private void updateGoalSetting(Member member, Integer dailyGoal, GoalRetention retention, LocalDate today) {
		if (dailyGoal == null) {
			member.applyGoalSetting(null, null, today);
			member.clearPendingGoalSetting();
			return;
		}

		GoalRetention resolvedRetention = resolveRetention(member, retention);
		LocalDate applyDate = goalMaintenanceService.resolveApplyDate(today);

		if (applyDate.isEqual(today)) {
			// 오늘 적용 가능한 경우: 동일 설정이면 skip
			if (isSameGoalSetting(member, dailyGoal, resolvedRetention) && member.getPendingApplyDate() == null) {
				return;
			}
			member.applyGoalSetting(dailyGoal, resolvedRetention, applyDate);
			member.clearPendingGoalSetting();
		} else {
			// 다음 주 예약 적용
			if (isSamePendingSetting(member, dailyGoal, resolvedRetention, applyDate)) {
				return;
			}
			member.scheduleGoalSetting(dailyGoal, resolvedRetention, applyDate);
		}
	}

	private GoalRetention resolveRetention(Member member, GoalRetention retention) {
		if (retention != null) {
			return retention;
		}
		// 요청에 유지기간이 없으면 기존 값, 없으면 CONTINUE
		return Optional.ofNullable(member.getGoalRetention()).orElse(GoalRetention.CONTINUE);
	}

	private boolean isSameGoalSetting(Member member, Integer dailyGoal, GoalRetention retention) {
		return Objects.equals(member.getDailyGoal(), dailyGoal)
			&& Objects.equals(member.getGoalRetention(), retention);
	}

	private boolean isSamePendingSetting(Member member, Integer dailyGoal, GoalRetention retention, LocalDate applyDate) {
		return Objects.equals(member.getPendingDailyGoal(), dailyGoal)
			&& Objects.equals(member.getPendingGoalRetention(), retention)
			&& Objects.equals(member.getPendingApplyDate(), applyDate);
	}

	private OnboardingResponseDto toOnboardingResponse(List<OnboardingResponseDto.Selection> selections, Member member) {
		return toOnboardingResponse(selections, member == null ? null : member.getDailyGoal(), member);
	}

	private OnboardingResponseDto toOnboardingResponse(
		List<OnboardingResponseDto.Selection> selections,
		Integer dailyGoal,
		Member member
	) {
		return OnboardingResponseDto.of(
			selections,
			dailyGoal,
			member == null ? null : member.getGoalRetention(),
			member == null ? null : member.getGoalEndDate(),
			member == null ? null : member.getPendingDailyGoal(),
			member == null ? null : member.getPendingGoalRetention(),
			member == null ? null : member.getPendingApplyDate()
		);
	}

	/**
	 * 관심사 반영 로직 (핵심)
	 *
	 * 프론트 계약: "최종 상태"를 통째로 보낸다 (Replace)
	 * 서버 역할: DB의 기존 상태와 비교해서 "차이만" 삭제/추가해 DB를 최종 상태로 맞춘다 (Smart Sync)
	 *
	 * 예시:
	 * - 기존 DB: [10, 11, 25]
	 * - 요청 값: [10, 30]
	 * => 삭제: [11, 25], 추가: [30]
	 */
	private void updateMemberInterestsSmartSync(Member member, List<OnboardingPatchRequestDto.Selection> selections) {


		// 0) NPE 방지 + 기본 형태 검증 (stream 전에 null 요소 검증)
		if (selections == null || selections.isEmpty() || selections.stream().anyMatch(Objects::isNull)) {
			throw new InterestException(InterestErrorCode.ONBOARDING_SELECTION_REQUIRED);
		}

		// 1) selection 구조 검증 + null subId 방지
		for (OnboardingPatchRequestDto.Selection sel : selections) {
			if (sel.interestId() == null || sel.subInterestIds() == null || sel.subInterestIds().isEmpty()) {
				throw new InterestException(InterestErrorCode.ONBOARDING_SELECTION_REQUIRED);
			}
			if (sel.subInterestIds().stream().anyMatch(Objects::isNull)) {
				throw new InterestException(InterestErrorCode.ONBOARDING_SELECTION_REQUIRED);
			}
		}

		// 2) 요청된 subInterestId 전체 집계(Set으로 중복 제거)
		Set<Long> requestedSubIds = selections.stream()
			.flatMap(sel -> sel.subInterestIds().stream())
			.collect(Collectors.toSet());

		if (requestedSubIds.isEmpty()) {
			throw new InterestException(InterestErrorCode.ONBOARDING_SELECTION_REQUIRED);
		}

		// 3) DB에서 (interestId, subId)만 조회하여
		//    - 존재 검증(pairs.size 비교)
		//    - 소속 검증(요청 interestId vs DB interestId)
		List<SubInterestRepository.InterestSubPair> pairs =
			subInterestRepository.findInterestSubPairsBySubIds(requestedSubIds);

		// 요청한 subIds 중 DB에 없는 값이 있으면 조회 개수가 줄어든다
		if (pairs.size() != requestedSubIds.size()) {
			throw new InterestException(InterestErrorCode.SUB_INTEREST_NOT_FOUND);
		}

		// subId -> interestId 매핑 구성(검증 시 Map lookup으로 처리)
		Map<Long, Long> subIdToInterestId = pairs.stream()
			.collect(Collectors.toMap(
				SubInterestRepository.InterestSubPair::getSubInterestId,
				SubInterestRepository.InterestSubPair::getInterestId
			));

		// 4) 소속 검증: 각 subId가 실제로 해당 interestId 소속인지 확인
		for (OnboardingPatchRequestDto.Selection sel : selections) {
			for (Long subId : sel.subInterestIds()) {
				Long actualInterestId = subIdToInterestId.get(subId);
				if (!Objects.equals(sel.interestId(), actualInterestId)) {
					throw new InterestException(InterestErrorCode.SUB_INTEREST_MISMATCH_INTEREST);
				}
			}
		}

		// 5) Smart Sync 계산: 기존 vs 요청 비교
		// existing = DB 상태, requested = 요청 최종 상태
		Set<Long> existingSubIds = new HashSet<>(
			memberSubInterestRepository.findSubInterestIdsByMemberId(member.getId())
		);

		// 삭제 대상 = existing - requested
		Set<Long> toDelete = new HashSet<>(existingSubIds);
		toDelete.removeAll(requestedSubIds);

		// 추가 대상 = requested - existing
		Set<Long> toAdd = new HashSet<>(requestedSubIds);
		toAdd.removeAll(existingSubIds);

		// 6) DB 반영(차이만)
		if (!toDelete.isEmpty()) {
			memberSubInterestRepository.deleteByMemberIdAndSubInterestIdIn(member.getId(), toDelete);
		}

		if (!toAdd.isEmpty()) {
			// 이미 존재 검증 완료(pairs.size 비교) 했으므로 getReferenceById 사용 가능
			List<MemberSubInterest> mappings = toAdd.stream()
				.map(subId -> MemberSubInterest.of(member, subInterestRepository.getReferenceById(subId)))
				.toList();
			memberSubInterestRepository.saveAll(mappings);
		}
		// 관심사 저장 성공 시 온보딩 완료로 처리 (목표 설정은 선택)
		member.completeOnboarding();
	}

	/**
	 * 응답 selections 만들기
	 * - (interestId, subId) pair로 가져온 뒤 interestId 기준으로 그룹핑하여
	 *   selections: [{interestId, [subIds...]}, ...] 형태로 구성
	 */
	private List<OnboardingResponseDto.Selection> loadSelections(Long memberId) {
		List<MemberSubInterestRepository.InterestSubPair> pairs =
			memberSubInterestRepository.findInterestSubPairsByMemberId(memberId);

		Map<Long, List<Long>> grouped = pairs.stream()
			.collect(Collectors.groupingBy(
				MemberSubInterestRepository.InterestSubPair::getInterestId,
				Collectors.mapping(MemberSubInterestRepository.InterestSubPair::getSubInterestId, Collectors.toList())
			));

		return grouped.entrySet().stream()
			.sorted(Map.Entry.comparingByKey())
			.map(e -> new OnboardingResponseDto.Selection(
				e.getKey(),
				e.getValue().stream().distinct().sorted().toList()
			))
			.toList();
	}
}
