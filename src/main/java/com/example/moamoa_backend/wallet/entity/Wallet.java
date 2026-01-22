package com.example.moamoa_backend.wallet.entity;

import com.example.moamoa_backend.global.entity.BaseEntity;
import com.example.moamoa_backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(nullable = false)
    private Integer point;

    private Wallet(Member member, Integer point) {
        this.member = member;
        this.point = point;
    }

    public static Wallet create(Member member) {
        return new Wallet(member, 0);
    }

    public void addPoint(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
        this.point += amount;
    }
}
