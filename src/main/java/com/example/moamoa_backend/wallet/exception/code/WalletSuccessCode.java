package com.example.moamoa_backend.wallet.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum WalletSuccessCode implements BaseSuccessCode {
    WALLET_CHARGE_SUCCESS(
            HttpStatus.OK,
            "WALLET200_1",
            "도토리가 성공적으로 적립되었습니다."
    ),

    WALLET_ATTENDANCE_REWARD_SUCCESS(
            HttpStatus.OK,
            "WALLET200_2",
            "출석 보상이 지급되었습니다."
    ),

    WALLET_MISSION_REWARD_SUCCESS(
            HttpStatus.OK,
            "WALLET200_3",
            "미션 보상이 지급되었습니다."
    ),

    WALLET_ADVERTISEMENT_REWARD_SUCCESS(
            HttpStatus.OK,
            "WALLET200_4",
            "광고 보상이 지급되었습니다."
    ),

    // ===== 조회 =====
    WALLET_BALANCE_INQUIRY_SUCCESS(
            HttpStatus.OK,
            "WALLET200_5",
                    "지갑 잔액 조회에 성공했습니다."
    ),

    WALLET_HISTORY_INQUIRY_SUCCESS(
            HttpStatus.OK,
            "WALLET200_6",
                    "도토리 내역 조회에 성공했습니다."
    ),
    WALLET_DAILY_HISTORY_SUCCESS(
            HttpStatus.OK,
        "WALLET_200_7",
                "도토리 일일 획득 내역 조회에 성공했습니다."
    );
    private final HttpStatus status;
    private final String code;
    private final String message;
}
