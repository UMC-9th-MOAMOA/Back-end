package com.example.moamoa_backend.wallet.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum WalletErrorCode implements BaseErrorCode {

    // ===== WalletHistory 관련 =====
    MISSION_REQUIRED_FOR_MISSION_TYPE(
            HttpStatus.BAD_REQUEST,
            "WALLET_400_001",
            "미션 보상(TransactionType.MISSION)은 반드시 mission이 필요합니다."
    ),

    MISSION_NOT_ALLOWED_FOR_NON_MISSION_TYPE(
            HttpStatus.BAD_REQUEST,
            "WALLET_400_002",
            "미션 보상이 아닌 경우 mission을 포함할 수 없습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
