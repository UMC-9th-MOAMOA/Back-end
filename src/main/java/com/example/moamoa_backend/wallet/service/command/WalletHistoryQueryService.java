package com.example.moamoa_backend.wallet.service.command;

import com.example.moamoa_backend.wallet.dto.WalletHistoryDayResponseDto;

import java.time.LocalDate;

public interface WalletHistoryQueryService {
    WalletHistoryDayResponseDto.Response getDayHistory(Long memberId, LocalDate date);
}
