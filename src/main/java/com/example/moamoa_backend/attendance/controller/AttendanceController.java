package com.example.moamoa_backend.attendance.controller;

import com.example.moamoa_backend.attendance.dto.AttendanceResponseDto;
import com.example.moamoa_backend.attendance.exception.code.AttendanceSuccessCode;
import com.example.moamoa_backend.attendance.service.command.AttendanceCommandService;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Attendance", description = "출석 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/attendance")
public class AttendanceController {
    private final AttendanceCommandService attendanceCommandService;

    @Operation(
            summary = "출석 체크",
            description = """
                    앱 진입 시 호출되는 출석 체크 API입니다.
                    
                    - 오늘 처음 출석이면: 출석 기록 저장 + 도토리 1개 지급
                    - 연속 7일 달성 시: 추가 보너스 도토리 10개 지급
                    - 이미 출석한 경우: '이미 출석했습니다' 에러 반환
                    """
    )
    @PostMapping
    public ApiResponse<AttendanceResponseDto.CheckInResult> checkIn(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long memberId = Long.parseLong(userDetails.getUsername());
        AttendanceResponseDto.CheckInResult result = attendanceCommandService.checkIn(memberId);
        return ApiResponse.onSuccess(
                AttendanceSuccessCode.ATTENDANCE_CHECK_IN_SUCCESS,
                result
        );
    }
}
