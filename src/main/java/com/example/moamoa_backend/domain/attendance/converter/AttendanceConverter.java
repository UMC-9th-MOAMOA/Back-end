package com.example.moamoa_backend.attendance.converter;

import com.example.moamoa_backend.attendance.dto.AttendanceResponseDto;

import java.time.LocalDate;

public class AttendanceConverter {
    private AttendanceConverter() {}

    public static AttendanceResponseDto.CheckInResult toCheckInResult(
            LocalDate date,
            boolean attendedToday,
            int streak,
            boolean completed7
    ) {
        return new AttendanceResponseDto.CheckInResult(date, attendedToday, streak, completed7);
    }
}
