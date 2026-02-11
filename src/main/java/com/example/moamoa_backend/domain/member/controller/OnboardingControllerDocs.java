package com.example.moamoa_backend.domain.member.controller;

import com.example.moamoa_backend.domain.member.dto.req.OnboardingPatchRequestDto;
import com.example.moamoa_backend.domain.member.dto.res.OnboardingResponseDto;
import com.example.moamoa_backend.domain.member.enums.OnboardingUpdateScope;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Onboarding", description = "온보딩/설정(관심사, 목표) API")
public interface OnboardingControllerDocs {

	@Operation(
		summary = "내 온보딩 조회",
		description = """
			내 온보딩 정보를 조회합니다. scope에 따라 필요한 항목만 반환합니다.<br><br>
			
			**[인증 필요]**<br>
			Authorization: Bearer {accessToken}<br><br>
			
			**[Query Params]**<br>
			• scope (기본값: ALL)<br><br>
			
			**[scope 별 반환]**<br>
			• ALL: selections + goalEnabled + dailyMissionGoal + goalRetention + goalEndDate + pendingGoal*<br>
			• INTERESTS: selections<br>
			• GOAL: goalEnabled + dailyMissionGoal + goalRetention + goalEndDate + pendingGoal*
			"""
	)
	ApiResponse<OnboardingResponseDto> getOnboarding(
		@AuthenticationPrincipal UserDetails userDetails,
		@RequestParam(defaultValue = "ALL") OnboardingUpdateScope scope
	);

	@Operation(
		summary = "내 온보딩 수정",
		description = """
			프론트는 사용자가 선택을 마친 **최종 결과 전체**를 전송합니다.<br>
			서버는 기존 저장값과 비교하여 DB 상태를 **최종 결과와 동일하게 갱신(동기화)** 합니다.<br><br>
			
			**[인증 필요]**<br>
			Authorization: Bearer {accessToken}<br><br>
			
			**[요청 조건]**<br>
			• scope로 수정 범위를 지정합니다.<br><br>
			
			**[scope 별 규칙]**<br>
			• ALL:<br>
			&nbsp;&nbsp;• selections 필수<br>
			&nbsp;&nbsp;• dailyMissionGoal/goalRetention 선택(null 허용)<br>
			&nbsp;&nbsp;• dailyMissionGoal=null AND goalRetention=null 이면 목표는 변경하지 않음(‘나중에 설정’)<br><br>
			
			• INTERESTS:<br>
			&nbsp;&nbsp;• selections 필수<br><br>
			
			• GOAL:<br>
			&nbsp;&nbsp;• OFF: goalEnabled=false<br>
			&nbsp;&nbsp;• ON/변경: goalEnabled=true + dailyMissionGoal(처음 켤 때 필요) + goalRetention(optional / retention-only 허용)
			"""
	)
	ApiResponse<OnboardingResponseDto> patchOnboarding(
		@AuthenticationPrincipal UserDetails userDetails,
		@RequestParam OnboardingUpdateScope scope,
		@Valid @RequestBody OnboardingPatchRequestDto request
	);
}
