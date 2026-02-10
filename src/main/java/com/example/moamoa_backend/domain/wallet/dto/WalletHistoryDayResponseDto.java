package com.example.moamoa_backend.wallet.dto;

import com.example.moamoa_backend.wallet.enums.TransactionType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class WalletHistoryDayResponseDto {
    public record Item(
            TransactionType type,
            int amount,                 // 도토리 증감(+) 기준
            LocalDateTime occurredAt,    // createdAt
            String missionTitle,         // type==MISSION 일 때만
            Integer missionDurationMinutes // type==MISSION 일 때만
    ) {}

    public record Response(
            LocalDate date,
            List<Item> items,
            int totalMinutes,
            int totalAcorns
    ) {}
}
