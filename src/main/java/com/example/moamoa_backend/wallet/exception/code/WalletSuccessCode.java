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
            "WALLET_200_001",
            "도토리가 성공적으로 적립되었습니다."
    ),

    WALLET_ATTENDANCE_REWARD_SUCCESS(
            HttpStatus.OK,
            "WALLET_200_002",
            "출석 보상이 지급되었습니다."
    ),

    WALLET_MISSION_REWARD_SUCCESS(
            HttpStatus.OK,
            "WALLET_200_003",
            "미션 보상이 지급되었습니다."
    ),

    WALLET_ADVERTISEMENT_REWARD_SUCCESS(
            HttpStatus.OK,
            "WALLET_200_004",
            "광고 보상이 지급되었습니다."
    ),

    // ===== 조회 =====
    WALLET_BALANCE_INQUIRY_SUCCESS(
            HttpStatus.OK,
            "WALLET_200_005",
                    "지갑 잔액 조회에 성공했습니다."
    ),

    WALLET_HISTORY_INQUIRY_SUCCESS(
            HttpStatus.OK,
            "WALLET_200_006",
                    "도토리 내역 조회에 성공했습니다."
    );
    private final HttpStatus status;
    private final String code;
    private final String message;
}
