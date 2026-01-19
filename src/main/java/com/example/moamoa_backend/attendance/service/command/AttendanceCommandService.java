package com.example.moamoa_backend.attendance.service.command;

import com.example.moamoa_backend.attendance.dto.AttendanceResponseDto;

public interface AttendanceCommandService {
    AttendanceResponseDto.CheckInResult checkIn(Long memberId);
}
