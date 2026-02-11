package com.example.moamoa_backend.domain.member.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.domain.member.dto.req.OnboardingPatchRequestDto;
import com.example.moamoa_backend.domain.member.dto.res.OnboardingResponseDto;
import com.example.moamoa_backend.domain.member.enums.OnboardingUpdateScope;
import com.example.moamoa_backend.domain.member.exception.code.MemberSuccessCode;
import com.example.moamoa_backend.domain.member.service.OnboardingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/me")
public class OnboardingController implements OnboardingControllerDocs {

	private final OnboardingService onboardingService;

	@Override
	@GetMapping("/onboarding")
	public ApiResponse<OnboardingResponseDto> getOnboarding(
		@AuthenticationPrincipal UserDetails userDetails,
		@RequestParam(defaultValue = "ALL") OnboardingUpdateScope scope
	) {
		Long memberId = Long.parseLong(userDetails.getUsername());

		return ApiResponse.onSuccess(
			MemberSuccessCode.MEMBER_GET_ONBOARDING,
			onboardingService.getMyOnboarding(memberId, scope)
		);
	}

	@Override
	@PatchMapping("/onboarding")
	public ApiResponse<OnboardingResponseDto> patchOnboarding(
		@AuthenticationPrincipal UserDetails userDetails,
		@RequestParam OnboardingUpdateScope scope,
		@Valid @RequestBody OnboardingPatchRequestDto request
	) {
		Long memberId = Long.parseLong(userDetails.getUsername());

		return ApiResponse.onSuccess(
			MemberSuccessCode.MEMBER_UPDATE_ONBOARDING,
			onboardingService.patchOnboarding(memberId, scope, request)
		);
	}
}
