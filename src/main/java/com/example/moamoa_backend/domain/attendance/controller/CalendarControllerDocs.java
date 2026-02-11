package com.example.moamoa_backend.domain.attendance.controller;

import com.example.moamoa_backend.domain.attendance.dto.AttendanceMonthResponseDto;
import com.example.moamoa_backend.domain.wallet.dto.WalletHistoryDayResponseDto;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Tag(name = "Calendar API", description = "스페이스 캘린더 관련 API")
public interface CalendarControllerDocs {
    @Operation(
            summary = "월별 출석/미션 보상 현황 조회",
            description = """
                    월 캘린더 표시용 API입니다.<br><br>

                    **[인증 필요]**<br>
                    Authorization: Bearer {accessToken}<br><br>

                    **[응답 필드]**<br>
                    - attendedDays: 출석한 날짜 리스트<br>
                    - missionRewardDays: 미션 보상(도토리 획득) 날짜 리스트<br>
                    """
    )
    ApiResponse<AttendanceMonthResponseDto.Response> getMonthStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam
            @Min(value = 1900, message = "연도는 1900년 이상이어야 합니다")
            @Max(value = 2100, message = "연도는 2100년 이하여야 합니다")
            int year,
            @RequestParam
            @Min(value = 1, message = "월은 1 이상이어야 합니다")
            @Max(value = 12, message = "월은 12 이하여야 합니다")
            int month
    );

    @Operation(
            summary = "일별 도토리 내역 조회",
            description = """
                    캘린더에서 특정 날짜 선택 시 해당 날짜의 도토리 내역을 조회합니다.<br><br>

                    **[인증 필요]**<br>
                    Authorization: Bearer {accessToken}<br><br>

                    **[응답 규칙]**<br>
                    - 출석/광고: type, amount 반환<br>
                    - 미션: type, amount + title + durationMinutes 반환<br>
                    """
    )
    ApiResponse<WalletHistoryDayResponseDto.Response> getDayHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    );
}
