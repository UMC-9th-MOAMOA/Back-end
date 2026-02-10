package com.example.moamoa_backend.domain.attendance.service.command;

import com.example.moamoa_backend.domain.attendance.dto.AttendanceResponseDto;

public interface AttendanceCommandService {
    AttendanceResponseDto.CheckInResult checkIn(Long memberId);
}
