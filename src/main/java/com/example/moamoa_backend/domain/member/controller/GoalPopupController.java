package com.example.moamoa_backend.domain.member.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.domain.member.dto.res.GoalPopupResponseDto;
import com.example.moamoa_backend.domain.member.exception.code.MemberSuccessCode;
import com.example.moamoa_backend.domain.member.service.GoalResultService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/me")
public class GoalPopupController implements GoalPopupControllerDocs {

	private final GoalResultService goalResultService;

	@Override
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

	@Override
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
