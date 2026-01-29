package com.example.moamoa_backend.wallet.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum WalletErrorCode implements BaseErrorCode {

    // ===== 공통 검증 =====
    INVALID_AMOUNT(
        HttpStatus.BAD_REQUEST,
        "WALLET_400_1",
        "amount는 1 이상이어야 합니다."
    ),

    // ===== Wallet 조회 =====
    WALLET_NOT_FOUND(
        HttpStatus.NOT_FOUND,
        "WALLET_404_1",
        "주머니 정보를 찾을 수 없습니다."
    ),

    // ===== 포인트 사용 =====
    INSUFFICIENT_POINTS(
        HttpStatus.BAD_REQUEST,
        "WALLET_400_2",
        "도토리가 부족합니다."
    ),

    // ===== WalletHistory 도메인 규칙 =====
    MISSION_REQUIRED_FOR_MISSION_TYPE(
        HttpStatus.BAD_REQUEST,
        "WALLET_400_3",
        "미션 보상(TransactionType.MISSION)은 반드시 mission이 필요합니다."
    ),

    MISSION_NOT_ALLOWED_FOR_NON_MISSION_TYPE(
        HttpStatus.BAD_REQUEST,
        "WALLET_400_4",
        "미션 보상이 아닌 경우 mission을 포함할 수 없습니다."
    ),
    ITEM_REQUIRED_FOR_PURCHASE_TYPE(
            HttpStatus.BAD_REQUEST,
            "WALLET_400_5",
            "구매(TransactionType.PURCHASE)는 반드시 item이 필요합니다."
    ),

    ITEM_NOT_ALLOWED_FOR_NON_PURCHASE_TYPE(
            HttpStatus.BAD_REQUEST,
            "WALLET_400_6",
            "구매가 아닌 경우 item을 포함할 수 없습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
