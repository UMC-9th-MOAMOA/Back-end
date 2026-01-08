package com.example.moamoa_backend.wallet.entity;

import com.example.moamoa_backend.global.entity.BaseEntity;
import com.example.moamoa_backend.wallet.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalletHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "wallet_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Wallet wallet;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private Integer balanceSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;
}
