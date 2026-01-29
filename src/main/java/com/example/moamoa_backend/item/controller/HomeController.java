package com.example.moamoa_backend.item.controller;

import com.example.moamoa_backend.item.dto.HomePocketResponseDto;
import com.example.moamoa_backend.item.service.query.MemberHomeQueryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.moamoa_backend.auth.exception.AuthException;
import com.example.moamoa_backend.auth.exception.code.AuthErrorCode;
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
	private final MemberHomeQueryService memberHomeQueryService;

	@Operation(summary = "홈 메인 조회", description = "사용자 이름/내 도토리 갯수/다람쥐 착장 정보(+배경 정보) 홈 메인 정보를 조회합니다.")
	@GetMapping("/home")
	public ApiResponse<HomeResponseDto> getHome(
		@AuthenticationPrincipal UserDetails userDetails
	) {
		Long memberId = Long.parseLong(userDetails.getUsername());
		HomeResponseDto result = homeService.getHome(memberId);

		return ApiResponse.onSuccess(ItemSuccessCode.HOME_OK, result);
	}

	@Operation(summary = "홈 화면 주머니 조회", description = "오늘/이번주 미션시간, 도토리 잔액, 연속출석, 목표 진행(없으면 null)")
	@GetMapping("/pocket")
	public ApiResponse<HomePocketResponseDto.Response> getHomePocket(
			@AuthenticationPrincipal UserDetails userDetails
	) {
		Long memberId = Long.parseLong(userDetails.getUsername());
		return ApiResponse.onSuccess(
				ItemSuccessCode.HOME_POCKET_OK,
				memberHomeQueryService.getHomePocket(memberId)
		);
	}
}