package com.example.moamoa_backend.wallet.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionType {
    // 포인트 증가 (적립)
    MISSION("미션 보상"),
    MISSION_COMPLETE("미션 완료 보상"),
    ATTENDANCE("출석 보상"),
    EVENT("이벤트 지급"),
    REFUND("구매 환불"),
    DAILY_REWARD("일일 보상"),
    WEEKLY_REWARD("주간 보상"),
    ATTENDANCE_STREAK_BONUS("연속 출석 보너스"),
    // 포인트 감소 (사용)
    PURCHASE("아이템 구매"),
    /*
    * 미션 재도전에 포인트 사용 등 추가할 소요 있음
     */

    // 관리/기타
    ADJUSTMENT("관리자 조정");   // 시스템 오류 등으로 관리자가 수동으로 수치를 고침

    private final String description;
}
