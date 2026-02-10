package com.example.moamoa_backend.domain.attendance.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AttendanceSuccessCode implements BaseSuccessCode {

    ATTENDANCE_CHECK_IN_SUCCESS(
            HttpStatus.OK,
            "ATTENDANCE_200_1",
            "출석 체크에 성공했습니다."
    ),
    ATTENDANCE_WEEK_STREAK_SUCCESS(
            HttpStatus.OK,
        "ATTENDANCE_200_2",
                "연속 출석 일수 조회에 성공했습니다."
    ),
    ATTENDANCE_MONTH_STATUS_SUCCESS(
            HttpStatus.OK,
            "ATTENDANCE_200_3",
            "월별 출석/미션 보상 현황 조회에 성공했습니다."
    );
    private final HttpStatus status;
    private final String code;
    private final String message;
}
