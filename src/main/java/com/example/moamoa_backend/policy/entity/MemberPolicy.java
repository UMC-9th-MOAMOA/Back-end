package com.example.moamoa_backend.policy.entity;

import com.example.moamoa_backend.global.entity.BaseEntity;
import com.example.moamoa_backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_member_policy",
                columnNames = {"member_id", "policy_id"}
        )
})
public class MemberPolicy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Column(nullable = false)
    private boolean isAgreed;

    @Column(nullable = true)
    private LocalDateTime agreedAt;

    public void updateAgreement(boolean isAgreed) {
        this.isAgreed = isAgreed;
        if (isAgreed) {
            this.agreedAt = LocalDateTime.now();
        }
    }
}
