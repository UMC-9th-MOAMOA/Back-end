package com.example.moamoa_backend.domain.wallet.controller;

import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.domain.wallet.dto.WalletHistoryListRequestDto;
import com.example.moamoa_backend.domain.wallet.dto.WalletHistoryListResponseDto;
import com.example.moamoa_backend.domain.wallet.dto.WalletPointResponseDto;
import com.example.moamoa_backend.domain.wallet.exception.code.WalletSuccessCode;
import com.example.moamoa_backend.domain.wallet.service.query.WalletHistoryQueryService;
import com.example.moamoa_backend.domain.wallet.service.query.WalletHistoryListQueryService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/me")
public class WalletHistoryController implements WalletHistoryControllerDocs {

	private final WalletHistoryListQueryService walletHistoryListQueryService;
	private final WalletHistoryQueryService walletHistoryQueryService;

	@Override
	@GetMapping("/wallet/history")
	public ApiResponse<WalletHistoryListResponseDto.Response> getWalletHistories(
		@AuthenticationPrincipal UserDetails userDetails,
		@RequestParam(defaultValue = "ALL") WalletHistoryListRequestDto.Tab tab,
		@RequestParam(defaultValue = "RECENT") WalletHistoryListRequestDto.Sort sort,
		@RequestParam(defaultValue = "ALL") WalletHistoryListRequestDto.Period period,
		@RequestParam(defaultValue = "ALL") WalletHistoryListRequestDto.EarnSource earnSource,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		Long memberId = Long.parseLong(userDetails.getUsername());

		return ApiResponse.onSuccess(
			WalletSuccessCode.WALLET_HISTORY_INQUIRY_SUCCESS,
			walletHistoryListQueryService.getHistories(memberId, tab, sort, period, earnSource, page, size)
		);
	}

	@Override
	@GetMapping("/wallet")
	public ApiResponse<WalletPointResponseDto.Response> getMyBalance(
		@AuthenticationPrincipal UserDetails userDetails
	) {
		Long memberId = Long.parseLong(userDetails.getUsername());
		return ApiResponse.onSuccess(
			WalletSuccessCode.WALLET_BALANCE_INQUIRY_SUCCESS,
			walletHistoryQueryService.getMyPoint(memberId)
		);
	}
}
