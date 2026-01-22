package com.example.moamoa_backend.wallet.entity;

import com.example.moamoa_backend.global.entity.BaseEntity;
import com.example.moamoa_backend.mission.entity.Mission;
import com.example.moamoa_backend.wallet.enums.TransactionType;
import com.example.moamoa_backend.wallet.exception.WalletException;
import com.example.moamoa_backend.wallet.exception.code.WalletErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id")
    private Mission mission;

    private WalletHistory(
            Wallet wallet,
            Mission mission,
            String description,
            Integer amount,
            Integer balanceSnapshot,
            TransactionType type
    ) {
        validate(type, mission);
        this.wallet = wallet;
        this.mission = mission;
        this.description = description;
        this.amount = amount;
        this.balanceSnapshot = balanceSnapshot;
        this.type = type;
    }

    public static WalletHistory create(
            Wallet wallet,
            Mission mission,
            String description,
            int amount,
            int balanceSnapshot,
            TransactionType type
    ) {
        return new WalletHistory(wallet, mission, description, amount, balanceSnapshot, type);
    }

    // ✅ 도메인 규칙:
    // - MISSION이면 mission 필수
    // - MISSION이 아니면 mission 금지
    private static void validate(TransactionType type, Mission mission) {
        if (type == TransactionType.MISSION && mission == null) {
            throw new WalletException(
                    WalletErrorCode.MISSION_REQUIRED_FOR_MISSION_TYPE
            );
        }
        if (type != TransactionType.MISSION && mission != null) {
            throw new WalletException(
                    WalletErrorCode.MISSION_NOT_ALLOWED_FOR_NON_MISSION_TYPE
            );
        }
    }
}
