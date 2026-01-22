package com.example.moamoa_backend.item.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.item.dto.HomeResponseDto;
import com.example.moamoa_backend.item.exception.code.ItemSuccessCode;
import com.example.moamoa_backend.item.service.HomeService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class HomeController {

	private final HomeService homeService;

	@Operation(summary = "홈 메인 조회", description = "미션 추천/재도전/주머니 API를 제외한 홈 메인 정보를 조회합니다.")
	@GetMapping("/home")
	public ApiResponse<HomeResponseDto> getHome(
		@AuthenticationPrincipal UserDetails userDetails
	) {
		Long memberId = extractMemberId(userDetails);
		HomeResponseDto result = homeService.getHome(memberId);

		return ApiResponse.onSuccess(ItemSuccessCode.HOME_OK, result);
	}

	private Long extractMemberId(UserDetails userDetails) {
		return Long.parseLong(userDetails.getUsername());
	}
}
