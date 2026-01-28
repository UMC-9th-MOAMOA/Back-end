package com.example.moamoa_backend.wallet.dto;

import com.example.moamoa_backend.item.enums.ItemType;
import com.example.moamoa_backend.wallet.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.List;

public class WalletHistoryListResponseDto {
    public record Response(
            int balance,
            List<Item> items
    ) {}

    public record Item(
            Long walletHistoryId,
            TransactionType type,
            int amount,
            LocalDateTime createdAt,
            String title,          // 구매=아이템명, 미션=미션명, 나머지=type.description
            String categoryLabel,  // "도토리 적립" | "도토리 사용"
            ItemType itemType      // 구매일 때만 값 존재(그 외 null)
    ) {}
}
