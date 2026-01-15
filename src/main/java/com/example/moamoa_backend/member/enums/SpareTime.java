package com.example.moamoa_backend.member.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SpareTime {
    SHORT("5~10분", 8),       // 화면용: "5~10분", 계산용: 8(분)
    MEDIUM("30분 내외", 30),    // 화면용: "30분 내외", 계산용: 30(분)
    LONG("1시간 이상", 60),     // 화면용: "1시간 이상", 계산용: 60(분)
    UNKNOWN("잘 모르겠어요", null); // 계산용: 0 (또는 null 처리)

    private final String description; // 프론트에 보여줄 설명
    private final Integer minutes;    // 로직 계산에 쓸 분 단위 시간
}
