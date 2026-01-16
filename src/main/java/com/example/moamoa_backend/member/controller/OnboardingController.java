package com.example.moamoa_backend.member.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.member.dto.OnboardingPatchRequestDto;
import com.example.moamoa_backend.member.dto.OnboardingResponseDto;
import com.example.moamoa_backend.member.enums.OnboardingUpdateScope;
import com.example.moamoa_backend.member.exception.code.MemberSuccessCode;
import com.example.moamoa_backend.member.service.OnboardingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/me")
@Tag(name = "Onboarding", description = "온보딩/설정(관심사, 목표) API")
public class OnboardingController {

	private final OnboardingService onboardingService;

	@Operation(
		summary = "내 온보딩 조회",
		description = """
            내 온보딩 정보를 조회합니다. scope에 따라 필요한 항목만 반환합니다.
            - ALL: selections + dailyMissionGoal
            - INTERESTS: selections
            - GOAL: dailyMissionGoal
            """
	)
	@GetMapping("/onboarding")
	public ApiResponse<OnboardingResponseDto> getOnboarding(
		@RequestHeader("X-MEMBER-ID") Long memberId, // TODO: 인증 적용 후 @AuthenticationPrincipal로 대체
		@RequestParam(defaultValue = "ALL") OnboardingUpdateScope scope
	) {
		return ApiResponse.onSuccess(
			MemberSuccessCode.MEMBER_GET_ONBOARDING,
			onboardingService.getMyOnboarding(memberId, scope)
		);
	}

	@Operation(
		summary = "내 온보딩 수정",
		description = """
            프론트는 사용자가 선택을 마친 최종 결과 전체를 전송합니다.
            서버는 기존 저장값과 비교하여 DB 상태를 최종 결과와 동일하게 갱신합니다(동기화).
            
            scope로 수정 범위를 지정합니다.
            - ALL: selections 필수, dailyMissionGoal은 선택(null 허용)
            - INTERESTS: selections 필수
            - GOAL: dailyMissionGoal 필수(0~5)
            """
	)
	@PatchMapping("/onboarding")
	public ApiResponse<OnboardingResponseDto> patchOnboarding(
		@RequestHeader("X-MEMBER-ID") Long memberId, // TODO: 인증 적용 후 @AuthenticationPrincipal로 대체
		@RequestParam OnboardingUpdateScope scope,
		@Valid @RequestBody OnboardingPatchRequestDto request
	) {
		return ApiResponse.onSuccess(
			MemberSuccessCode.MEMBER_UPDATE_ONBOARDING,
			onboardingService.patchOnboarding(memberId, scope, request)
		);
	}
}

