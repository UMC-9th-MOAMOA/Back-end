package com.example.moamoa_backend.domain.policy.entity;

import com.example.moamoa_backend.global.entity.BaseEntity;
import com.example.moamoa_backend.domain.policy.enums.PolicyType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 약관 엔티티
 * - 서비스 이용약관, 개인정보처리방침 등 각종 약관 정보 관리
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Policy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyType policyType;

    @Column(nullable = false)
    private boolean isMandatory;

    @Column(nullable = false)
    private String version;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime effectiveAt;

    @Column(nullable = false)
    private boolean isActive = true;
}
