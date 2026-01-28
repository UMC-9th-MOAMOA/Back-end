package com.example.moamoa_backend.wallet.service.query;

import com.example.moamoa_backend.wallet.dto.WalletHistoryListRequestDto;
import com.example.moamoa_backend.wallet.dto.WalletHistoryListResponseDto;

public interface WalletHistoryListQueryService {
    WalletHistoryListResponseDto.Response getHistories(
            Long memberId,
            WalletHistoryListRequestDto.Tab tab,
            WalletHistoryListRequestDto.Sort sort,
            WalletHistoryListRequestDto.Period period,
            WalletHistoryListRequestDto.EarnSource earnSource,
            int page,   // ✅ 추가 (1부터)
            int size    // ✅ 추가
    );
}
