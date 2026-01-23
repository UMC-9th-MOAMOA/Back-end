package com.example.moamoa_backend.member.entity;

import com.example.moamoa_backend.global.entity.BaseEntity;
import com.example.moamoa_backend.member.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(uniqueConstraints = {
        // [하나의 소셜 로그인 계정으로 하나의 서비스 계정 보장]
        // 소셜 로그인 계정은 email이 null일 수 있기 때문에 email 관련된 제약조건을 추가할 수 없음
        // 이에 따라 LOCAL 계정의 providerId에는 email을 넣어서 해당 문제 해결
        @UniqueConstraint(
                name = "uk_member_provider_id",
                columnNames = {"provider", "providerId"}
        )
})
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true, length = 100)
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

    @Column(nullable = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Gender gender;

    @Column(nullable = true)
    private LocalDate birthday;

    public void updateDailyGoal(Integer dailyGoal) {
        this.dailyGoal = dailyGoal;
        this.weeklyGoal = (dailyGoal == null) ? null : dailyGoal * 7;
    }

}
