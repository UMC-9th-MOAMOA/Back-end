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

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/space/calendar")
public class CalendarController implements CalendarControllerDocs{

    private final AttendanceQueryService attendanceQueryService;
    private final WalletHistoryQueryService walletHistoryQueryService;

    @Override
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

    @Override
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
