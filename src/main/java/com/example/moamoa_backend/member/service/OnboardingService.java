package com.example.moamoa_backend.member.service;

import com.example.moamoa_backend.interest.exception.InterestException;
import com.example.moamoa_backend.interest.exception.code.InterestErrorCode;
import com.example.moamoa_backend.interest.repository.SubInterestRepository;
import com.example.moamoa_backend.member.dto.OnboardingPatchRequestDto;
import com.example.moamoa_backend.member.dto.OnboardingResponseDto;
import com.example.moamoa_backend.member.entity.Member;
import com.example.moamoa_backend.member.entity.MemberSubInterest;
import com.example.moamoa_backend.member.enums.OnboardingUpdateScope;
import com.example.moamoa_backend.member.exception.MemberException;
import com.example.moamoa_backend.member.exception.code.MemberErrorCode;
import com.example.moamoa_backend.member.repository.MemberRepository;
import com.example.moamoa_backend.member.repository.MemberSubInterestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OnboardingService {

	private final MemberRepository memberRepository;
	private final MemberSubInterestRepository memberSubInterestRepository;
	private final SubInterestRepository subInterestRepository;

	/**
	 * 온보딩 수정 API
	 */
	@Transactional
	public OnboardingResponseDto patchOnboarding(Long memberId, OnboardingUpdateScope scope, OnboardingPatchRequestDto req) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

		return switch (scope) {
			case ALL -> {
				requireSelections(req.selections());
				validateGoalRangeIfPresent(req.dailyMissionGoal());

				updateMemberInterestsSmartSync(member, req.selections());
				member.updateDailyGoal(req.dailyMissionGoal()); // nullable 허용

				yield getMyOnboarding(memberId, OnboardingUpdateScope.ALL);
			}
			case INTERESTS -> {
				requireSelections(req.selections());
				updateMemberInterestsSmartSync(member, req.selections());
				yield getMyOnboarding(memberId, OnboardingUpdateScope.ALL);
			}
			case GOAL -> {
				requireGoal(req.dailyMissionGoal());
				validateGoalRange(req.dailyMissionGoal());
				member.updateDailyGoal(req.dailyMissionGoal());
				yield getMyOnboarding(memberId, OnboardingUpdateScope.ALL);
			}
			default -> throw new MemberException(MemberErrorCode.INVALID_SCOPE);
		};
	}

	/**
	 * 온보딩 조회 API
	 */
	@Transactional(readOnly = true)
	public OnboardingResponseDto getMyOnboarding(Long memberId, OnboardingUpdateScope scope) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

		return switch (scope) {
			case ALL -> OnboardingResponseDto.of(loadSelections(memberId), member.getDailyGoal());
			case INTERESTS -> OnboardingResponseDto.of(loadSelections(memberId), null);
			case GOAL -> OnboardingResponseDto.of(null, member.getDailyGoal());
			default -> throw new MemberException(MemberErrorCode.INVALID_SCOPE);
		};
	}

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

	private void validateGoalRange(Integer goal) {
		if (goal < 0 || goal > 5) {
			throw new InterestException(InterestErrorCode.ONBOARDING_GOAL_OUT_OF_RANGE);
		}
	}

	private void validateGoalRangeIfPresent(Integer goal) {
		if (goal != null) validateGoalRange(goal);
	}

	/**
	 * Replace(최종 리스트 전송) + Smart Sync(차이만 반영)
	 */
	private void updateMemberInterestsSmartSync(Member member, List<OnboardingPatchRequestDto.Selection> selections) {

		// 1) 요청 subInterestId 집계(중복 제거)
		Set<Long> requestedSubIds = selections.stream()
			.flatMap(s -> Optional.ofNullable(s.subInterestIds()).orElseGet(List::of).stream())
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		if (requestedSubIds.isEmpty()) {
			throw new InterestException(InterestErrorCode.ONBOARDING_SELECTION_REQUIRED);
		}

		// 2) 존재 검증 + (subId -> interestId) 매핑 확보
		List<SubInterestRepository.InterestSubPair> pairs =
			subInterestRepository.findInterestSubPairsBySubIds(requestedSubIds);

		if (pairs.size() != requestedSubIds.size()) {
			throw new InterestException(InterestErrorCode.SUB_INTEREST_NOT_FOUND);
		}

		Map<Long, Long> subIdToInterestId = pairs.stream()
			.collect(Collectors.toMap(
				SubInterestRepository.InterestSubPair::getSubInterestId,
				SubInterestRepository.InterestSubPair::getInterestId
			));

		// 3) 요청 구조 검증 + 소속 검증
		for (OnboardingPatchRequestDto.Selection sel : selections) {
			if (sel.interestId() == null || sel.subInterestIds() == null || sel.subInterestIds().isEmpty()) {
				throw new InterestException(InterestErrorCode.ONBOARDING_SELECTION_REQUIRED);
			}
			for (Long subId : sel.subInterestIds()) {
				Long actualInterestId = subIdToInterestId.get(subId);
				if (!Objects.equals(sel.interestId(), actualInterestId)) {
					throw new InterestException(InterestErrorCode.SUB_INTEREST_MISMATCH_INTEREST);
				}
			}
		}

		// 4) Smart Sync 계산
		Set<Long> existingSubIds = new HashSet<>(
			memberSubInterestRepository.findSubInterestIdsByMemberId(member.getId())
		);

		//삭제 대상
		Set<Long> toDelete = new HashSet<>(existingSubIds);
		toDelete.removeAll(requestedSubIds);

		//추가 대상
		Set<Long> toAdd = new HashSet<>(requestedSubIds);
		toAdd.removeAll(existingSubIds);

		// 5) DB 반영(차이만)
		if (!toDelete.isEmpty()) {
			memberSubInterestRepository.deleteByMemberIdAndSubInterestIdIn(member.getId(), toDelete);
		}

		if (!toAdd.isEmpty()) {
			List<MemberSubInterest> mappings = toAdd.stream()
				.map(subId -> MemberSubInterest.of(member, subInterestRepository.getReferenceById(subId)))
				.toList();
			memberSubInterestRepository.saveAll(mappings);
		}
	}

	/**
	 * 응답 selections 만들기
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
