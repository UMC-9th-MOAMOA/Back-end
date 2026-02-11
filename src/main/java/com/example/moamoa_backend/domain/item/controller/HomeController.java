package com.example.moamoa_backend.domain.item.controller;

import com.example.moamoa_backend.domain.item.dto.HomePocketResponseDto;
import com.example.moamoa_backend.domain.item.service.query.MemberHomeQueryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.domain.item.dto.HomeResponseDto;
import com.example.moamoa_backend.domain.item.exception.code.ItemSuccessCode;
import com.example.moamoa_backend.domain.item.service.HomeService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class HomeController implements HomeControllerDocs{

	private final HomeService homeService;
	private final MemberHomeQueryService memberHomeQueryService;

	@Override
	@GetMapping("/home")
	public ApiResponse<HomeResponseDto> getHome(
		@AuthenticationPrincipal UserDetails userDetails
	) {
		Long memberId = Long.parseLong(userDetails.getUsername());
		HomeResponseDto result = homeService.getHome(memberId);

		return ApiResponse.onSuccess(ItemSuccessCode.HOME_OK, result);
	}

	@Override
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