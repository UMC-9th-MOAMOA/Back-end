package com.example.moamoa_backend.domain.attendance.controller;

import com.example.moamoa_backend.domain.attendance.dto.AttendanceMonthResponseDto;
import com.example.moamoa_backend.domain.attendance.exception.code.AttendanceSuccessCode;
import com.example.moamoa_backend.domain.attendance.service.query.AttendanceQueryService;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.domain.wallet.dto.WalletHistoryDayResponseDto;
import com.example.moamoa_backend.domain.wallet.exception.code.WalletSuccessCode;
import com.example.moamoa_backend.domain.wallet.service.query.WalletHistoryQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "Calendar", description = "캘린더 API")
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/space/calendar")
public class CalendarController {

    private final AttendanceQueryService attendanceQueryService;
    private final WalletHistoryQueryService walletHistoryQueryService;

    @Operation(
            summary = "월별 출석/미션 보상 현황 조회",
            description = """
                    월 캘린더 표시용 API입니다.
                    - attendedDays: 출석한 일자(파란색 표시)
                    - missionRewardDays: 미션 보상으로 도토리 획득한 일자(도토리 아이콘 표시)
                    """
    )
    @GetMapping("/calendar")
    public ApiResponse<AttendanceMonthResponseDto.Response> getMonthStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam
            @Min(value = 1900, message = "연도는 1900년 이상이어야 합니다")
            @Max(value = 2100, message = "연도는 2100년 이하여야 합니다")
            int year,

            @RequestParam
            @Min(value = 1, message = "월은 1 이상이어야 합니다")
            @Max(value = 12, message = "월은 12 이하여야 합니다")
            int month
    ) {
        Long memberId = Long.parseLong(userDetails.getUsername());
        AttendanceMonthResponseDto.Response result =
                attendanceQueryService.getMonthStatus(memberId, year, month);

        return ApiResponse.onSuccess(
                AttendanceSuccessCode.ATTENDANCE_MONTH_STATUS_SUCCESS,
                result
        );
    }

    // ✅ 여기부터 추가: 일별 도토리 내역 조회 (캘린더 아래로 이동)
    @Operation(
            summary = "일별 도토리 내역 조회",
            description = """
                    캘린더에서 특정 날짜를 선택했을 때 해당 날짜의 도토리 내역을 조회합니다.
                    - 출석/광고: type, amount 반환
                    - 미션: type, amount + 미션 제목(title) + 소요시간(durationMinutes) 반환
                    """
    )
    @GetMapping("/day")
    public ApiResponse<WalletHistoryDayResponseDto.Response> getDayHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Long memberId = Long.parseLong(userDetails.getUsername());
        return ApiResponse.onSuccess(
                WalletSuccessCode.WALLET_DAILY_HISTORY_SUCCESS,
                walletHistoryQueryService.getDayHistory(memberId, date)
        );
    }
}
