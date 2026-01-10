package com.example.moamoa_backend.member.entity;

import com.example.moamoa_backend.global.entity.BaseEntity;
import com.example.moamoa_backend.member.enums.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_member_provider_id", // 제약조건 이름
                columnNames = {"provider", "providerId"} // 두 컬럼 조합 유일
        )
})
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = true)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(nullable = true)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @Column(nullable = true)
    private LocalDateTime deletedAt;

    @Column(nullable = true)
    private Integer dailyGoal;

    @Column(nullable = true)
    private Integer weeklyGoal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private SpareTime spareTime;

    @Column(nullable = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;
}
