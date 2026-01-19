package com.example.moamoa_backend.attendance.controller;

import com.example.moamoa_backend.attendance.dto.AttendanceResponseDto;
import com.example.moamoa_backend.attendance.exception.code.AttendanceSuccessCode;
import com.example.moamoa_backend.attendance.service.command.AttendanceCommandService;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/attendance")
public class AttendanceController {
    private final AttendanceCommandService attendanceCommandService;

    @PostMapping
    public ApiResponse<AttendanceResponseDto.CheckInResult> checkIn(
            @RequestParam Long memberId
    ) {
        AttendanceResponseDto.CheckInResult result = attendanceCommandService.checkIn(memberId);
        return ApiResponse.onSuccess(
                AttendanceSuccessCode.ATTENDANCE_CHECK_IN_SUCCESS,
                result
        );
    }
}
