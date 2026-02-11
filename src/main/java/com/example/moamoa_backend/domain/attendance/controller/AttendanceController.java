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


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/attendance")
public class AttendanceController implements AttendanceControllerDocs{
    private final AttendanceCommandService attendanceCommandService;
    private final AttendanceQueryService attendanceQueryService;

    @Override
    @PostMapping("/check")
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

    @Override
    @GetMapping("/week")
    public ApiResponse<AttendanceWeekResponseDto.Response> getWeekStreak(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long memberId = Long.parseLong(userDetails.getUsername());
        int streak = attendanceQueryService.getCurrentStreak(memberId);

        return ApiResponse.onSuccess(
                AttendanceSuccessCode.ATTENDANCE_WEEK_STREAK_SUCCESS,
                new AttendanceWeekResponseDto.Response(streak)
        );
    }

}
