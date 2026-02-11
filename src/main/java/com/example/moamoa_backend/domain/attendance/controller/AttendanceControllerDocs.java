package com.example.moamoa_backend.domain.attendance.controller;

import com.example.moamoa_backend.domain.attendance.dto.AttendanceResponseDto;
import com.example.moamoa_backend.domain.attendance.dto.AttendanceWeekResponseDto;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@Tag(name = "Attendance API", description = "출석 관련 API")
public interface AttendanceControllerDocs {
    @Operation(
            summary = "출석 체크",
            description = """
                    앱 진입 시 호출되는 출석 체크 API입니다.<br><br>

                    **[인증 필요]**<br>
                    Authorization: Bearer {accessToken}<br><br>

                    **[동작 방식]**<br>
                    - 오늘 첫 출석: 출석 기록 저장 + 도토리 1개 지급<br>
                    - 연속 7일 달성: 추가 보너스 도토리 10개 지급<br>
                    - 이미 출석한 경우: 에러 반환<br>
                    """
    )
    ApiResponse<AttendanceResponseDto.CheckInResult> checkIn(
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "연속 출석 일수 조회",
            description = """
                    연속 출석 일수(streak)만 조회합니다.<br><br>

                    **[인증 필요]**<br>
                    Authorization: Bearer {accessToken}
                    """
    )
    ApiResponse<AttendanceWeekResponseDto.Response> getWeekStreak(
            @AuthenticationPrincipal UserDetails userDetails
    );
}
