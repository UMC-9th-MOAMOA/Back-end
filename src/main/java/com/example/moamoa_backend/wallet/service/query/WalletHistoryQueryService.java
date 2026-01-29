package com.example.moamoa_backend.wallet.service.query;

import com.example.moamoa_backend.wallet.dto.WalletHistoryDayResponseDto;
import com.example.moamoa_backend.wallet.dto.WalletPointResponseDto;

import java.time.LocalDate;

public interface WalletHistoryQueryService {
    WalletHistoryDayResponseDto.Response getDayHistory(Long memberId, LocalDate date);
    WalletPointResponseDto.Response getMyPoint(Long memberId);
}
