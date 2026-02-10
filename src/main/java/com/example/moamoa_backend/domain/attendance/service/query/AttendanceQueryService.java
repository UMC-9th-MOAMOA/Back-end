package com.example.moamoa_backend.domain.attendance.service.query;

import com.example.moamoa_backend.domain.attendance.dto.AttendanceMonthResponseDto;

public interface AttendanceQueryService {
    int getCurrentStreak(Long memberId);

    // ✅ 월별 출석/미션 보상 날짜 조회
    AttendanceMonthResponseDto.Response getMonthStatus(Long memberId, int year, int month);
}
