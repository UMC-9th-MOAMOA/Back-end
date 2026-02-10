package com.example.moamoa_backend.domain.attendance.controller;

import com.example.moamoa_backend.domain.attendance.dto.AttendanceResponseDto;
import com.example.moamoa_backend.domain.attendance.dto.AttendanceWeekResponseDto;
import com.example.moamoa_backend.domain.attendance.exception.code.AttendanceSuccessCode;
import com.example.moamoa_backend.domain.attendance.service.command.AttendanceCommandService;
import com.example.moamoa_backend.domain.attendance.service.query.AttendanceQueryService;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Attendance", description = "출석 관련 API")
public interface AttendanceControllerDocs {
    @Operation(
            summary = "출석 체크",
            description = """
                    앱 진입 시 호출되는 출석 체크 API입니다.
                    
                    - 오늘 처음 출석이면: 출석 기록 저장 + 도토리 1개 지급
                    - 연속 7일 달성 시: 추가 보너스 도토리 10개 지급
                    - 이미 출석한 경우: '이미 출석했습니다' 에러 반환
                    """
    )
    public ApiResponse<AttendanceResponseDto.CheckInResult> checkIn(
            @AuthenticationPrincipal UserDetails userDetails
    ) ;

    @Operation(
            summary = "연속 출석 일수 조회",
            description = "팝업 외 다른 화면에서 연속 출석 일수(streak)만 필요할 때 사용하는 API입니다."
    )
    public ApiResponse<AttendanceWeekResponseDto.Response> getWeekStreak(
            @AuthenticationPrincipal UserDetails userDetails
    ) ;

}
