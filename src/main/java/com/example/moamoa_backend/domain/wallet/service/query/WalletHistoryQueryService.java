package com.example.moamoa_backend.domain.wallet.service.query;

import com.example.moamoa_backend.domain.wallet.dto.WalletHistoryDayResponseDto;
import com.example.moamoa_backend.domain.wallet.dto.WalletPointResponseDto;

import java.time.LocalDate;

public interface WalletHistoryQueryService {
    WalletHistoryDayResponseDto.Response getDayHistory(Long memberId, LocalDate date);
    WalletPointResponseDto.Response getMyPoint(Long memberId);
}
