package com.example.moamoa_backend.attendance.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AttendanceSuccessCode implements BaseSuccessCode {

    ATTENDANCE_CHECK_IN_SUCCESS(
            HttpStatus.OK,
            "ATTENDANCE_200",
            "출석 체크에 성공했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
