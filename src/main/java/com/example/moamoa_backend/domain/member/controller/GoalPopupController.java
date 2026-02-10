package com.example.moamoa_backend.domain.member.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.domain.member.dto.res.GoalPopupResponseDto;
import com.example.moamoa_backend.domain.member.exception.code.MemberSuccessCode;
import com.example.moamoa_backend.domain.member.service.GoalResultService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/me")
public class GoalPopupController  implements GoalPopupControllerDocs {

	private final GoalResultService goalResultService;


	/**
	 * [홈 진입 시 호출]
	 * 목표 '실패' 팝업만 조회한다.
	 *
	 *   정책:
	 * - 성공 팝업은 '답안 제출 즉시' 처리(별도 UX)하며, 홈에서는 노출하지 않는다.
	 * - 홈에서는 오직 "전일/전주 실패"만 조회한다.
	 *
	 *   조회 범위:
	 * - DAILY  : 어제(goalDate = yesterday)의 실패 결과 중, 아직 안 본(popupShownAt == null) 것
	 * - WEEKLY : 지난 주 일요일(goalDate = lastWeekEnd)의 실패 결과 중, 아직 안 본(popupShownAt == null) 것
	 *
	 *   반환 규칙:
	 * - 노출할 팝업이 없으면 빈 배열([])을 반환한다.
	 * - 팝업은 '조회만'으로는 소진되지 않으며,
	 *   프론트가 실제로 띄운 뒤 shown API를 호출해야 소진(봤음 처리)된다.
	 */
	@GetMapping("/goal-popups")
	public ApiResponse<GoalPopupResponseDto> getGoalPopups(
		@AuthenticationPrincipal UserDetails userDetails
	) {
		Long memberId = Long.parseLong(userDetails.getUsername());
		return ApiResponse.onSuccess(
			MemberSuccessCode.MEMBER_GET_GOAL_POPUPS,
			goalResultService.getGoalPopups(memberId)
		);
	}


	/**
	 * [프론트에서 팝업을 실제로 띄운 뒤 호출]
	 * 특정 goalResult 팝업을 "봤음"으로 처리한다.
	 *
	 *   권한/무결성:
	 * - goalResultId는 반드시 '내 것'이어야 한다. (id + memberId로 소유자 검증)
	 *
	 *   멱등성:
	 * - 이미 본 팝업(popupShownAt != null)을 다시 호출해도 상태는 유지된다.
	 */
	@PatchMapping("/goal-popups/{goalResultId}/shown")
	public ApiResponse<Void> markGoalPopupShown(
		@AuthenticationPrincipal UserDetails userDetails,
		@PathVariable Long goalResultId
	) {
		Long memberId = Long.parseLong(userDetails.getUsername());
		goalResultService.markPopupShown(memberId, goalResultId);
		return ApiResponse.onSuccess(
			MemberSuccessCode.MEMBER_MARK_GOAL_POPUP_SHOWN,
			null
		);
	}
}
