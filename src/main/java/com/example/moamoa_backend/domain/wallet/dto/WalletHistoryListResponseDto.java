package com.example.moamoa_backend.domain.wallet.dto;

import com.example.moamoa_backend.domain.item.enums.ItemType;
import com.example.moamoa_backend.domain.wallet.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.List;

public class WalletHistoryListResponseDto {
    public record Response(
            int balance,
            int page,              // ✅ 현재 페이지(1부터)
            int size,              // ✅ 페이지당 개수
            long totalElements,    // ✅ 전체 개수
            int totalPages,        // ✅ 전체 페이지 수
            boolean hasNext,       // ✅ 다음 페이지 존재 여부
            List<Item> items
    ) {}

    public record Item(
            Long walletHistoryId,
            TransactionType type,
            int amount,
            LocalDateTime createdAt,
            String title,
            String categoryLabel,
            ItemType itemType
    ) {}
}
