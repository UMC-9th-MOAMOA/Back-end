package com.example.moamoa_backend.domain.wallet.controller;

import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.domain.wallet.dto.WalletHistoryListRequestDto;
import com.example.moamoa_backend.domain.wallet.dto.WalletHistoryListResponseDto;
import com.example.moamoa_backend.domain.wallet.dto.WalletPointResponseDto;
import com.example.moamoa_backend.domain.wallet.exception.code.WalletSuccessCode;
import com.example.moamoa_backend.domain.wallet.service.query.WalletHistoryQueryService;
import com.example.moamoa_backend.domain.wallet.service.query.WalletHistoryListQueryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

public interface WalletHistoryControllerDocs {
    @Operation(
            summary = "도토리 히스토리 조회",
            description =
                    """
                    내 도토리 히스토리를 조회합니다.
                    - tab: ALL(전체) / EARN(적립) / USE(사용)
                    - sort: RECENT(최근순) / OLDEST(오래된순)
                    - period: ALL / THREE_MONTHS(3개월) / SIX_MONTHS(6개월)
                    - earnSource: ALL / MISSION / ATTENDANCE (적립 탭에서만 적용)
                      * 미션: MISSION + MISSION_COMPLETE
                      * 출석: ATTENDANCE + ATTENDANCE_STREAK_BONUS
                    """
    )
    public ApiResponse<WalletHistoryListResponseDto.Response> getWalletHistories(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "ALL") WalletHistoryListRequestDto.Tab tab,
            @RequestParam(defaultValue = "RECENT") WalletHistoryListRequestDto.Sort sort,
            @RequestParam(defaultValue = "ALL") WalletHistoryListRequestDto.Period period,
            @RequestParam(defaultValue = "ALL") WalletHistoryListRequestDto.EarnSource earnSource,

            // ✅ 추가
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) ;

    @Operation(summary = "내 도토리 잔액 조회", description = "Wallet.point(도토리 개수)만 반환합니다.")
    public ApiResponse<WalletPointResponseDto.Response> getMyBalance(
            @AuthenticationPrincipal UserDetails userDetails
    ) ;

}
