package com.example.moamoa_backend.domain.wallet.controller;

import com.example.moamoa_backend.domain.wallet.dto.WalletHistoryListRequestDto;
import com.example.moamoa_backend.domain.wallet.dto.WalletHistoryListResponseDto;
import com.example.moamoa_backend.domain.wallet.dto.WalletPointResponseDto;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Wallet API", description = "도토리(지갑) 관련 API")
public interface WalletHistoryControllerDocs {
	@Operation(
		summary = "도토리 히스토리 조회",
		description = """
			내 도토리 히스토리를 조회합니다.<br><br>
			
			**[인증 필요]**<br>
			Authorization: Bearer {accessToken}<br><br>
			
			**[필터]**<br>
			- tab: ALL / EARN / USE<br>
			- sort: RECENT / OLDEST<br>
			- period: ALL / THREE_MONTHS / SIX_MONTHS<br>
			- earnSource: ALL / MISSION / ATTENDANCE (EARN 탭에서만 적용)<br>
			  * 미션: MISSION + MISSION_COMPLETE 포함<br>
			  * 출석: ATTENDANCE + ATTENDANCE_STREAK_BONUS 포함<br><br>
			
			**[페이징]**<br>
			- page, size 기반 페이지네이션
			"""
	)
	ApiResponse<WalletHistoryListResponseDto.Response> getWalletHistories(
		@AuthenticationPrincipal UserDetails userDetails,
		@RequestParam(defaultValue = "ALL") WalletHistoryListRequestDto.Tab tab,
		@RequestParam(defaultValue = "RECENT") WalletHistoryListRequestDto.Sort sort,
		@RequestParam(defaultValue = "ALL") WalletHistoryListRequestDto.Period period,
		@RequestParam(defaultValue = "ALL") WalletHistoryListRequestDto.EarnSource earnSource,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size
	);

	@Operation(
		summary = "내 도토리 잔액 조회",
		description = """
			Wallet.point(도토리 잔액)만 조회합니다.<br><br>
			
			**[인증 필요]**<br>
			Authorization: Bearer {accessToken}
			"""
	)
	ApiResponse<WalletPointResponseDto.Response> getMyBalance(
		@AuthenticationPrincipal UserDetails userDetails
	);
}
