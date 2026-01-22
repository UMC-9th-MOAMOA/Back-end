package com.example.moamoa_backend.wallet.repository;

import com.example.moamoa_backend.wallet.entity.WalletHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletHistoryRepository extends JpaRepository<WalletHistory, Long> {
}
