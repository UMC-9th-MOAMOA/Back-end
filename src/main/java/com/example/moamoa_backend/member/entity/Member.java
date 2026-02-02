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

    /**
     * 목표 유지 기간(없으면 null) - 설정 시 종료일 계산에 사용
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private GoalRetention goalRetention;

    /**
     * 목표 유지 종료일(포함) - 오늘이 이 날짜를 지나면 목표가 만료
     */
    @Column(nullable = true)
    private LocalDate goalEndDate;


    /**
     * 다음 주 적용 대기 중인 목표 값
     */
    @Column(nullable = true)
    private Integer pendingDailyGoal;

    /**
     * 다음 주 적용 대기 중인 목표 유지 기간
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private GoalRetention pendingGoalRetention;

    /**
     * 다음 주 적용 예정일(월요일)
     */
    @Column(nullable = true)
    private LocalDate pendingApplyDate;

    @Column(nullable = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Gender gender;

    @Column(nullable = true)
    private LocalDate birthday;

    @Column(nullable = false)
    @Builder.Default
    private Integer profileImage = 1;

    @Column(nullable = false)
    @Builder.Default
    private Boolean onboardingCompleted = false;

    /**
     * 온보딩 완료 처리
     */
    public void completeOnboarding() {
        if (Boolean.TRUE.equals(this.onboardingCompleted)) return; // 이미 완료면 스킵
        this.onboardingCompleted = true;
    }

    /**
     * 목표 설정을 즉시 반영한다.
     * - dailyGoal이 null이면 목표 OFF 처리(관련 필드 초기화)
     * - dailyGoal이 있으면 weeklyGoal, retention, endDate까지 계산해 설정
     */
    public void applyGoalSetting(Integer dailyGoal, GoalRetention retention, LocalDate startDate) {

        if (dailyGoal != null && retention != null && startDate == null) {
                   throw new IllegalArgumentException("startDate는 retention 설정 시 필수입니다.");
                }

        if (dailyGoal == null) {
            this.dailyGoal = null;
            this.weeklyGoal = null;
            this.goalRetention = null;
            this.goalEndDate = null;
            return;
        }

        this.dailyGoal = dailyGoal;
        this.weeklyGoal = dailyGoal * 7;
        this.goalRetention = retention;
        this.goalEndDate = retention == null ? null : retention.calculateEndDate(startDate);
    }

    /**
     * 목표 변경을 다음 주 적용으로 예약한다.
     */
    public void scheduleGoalSetting(Integer dailyGoal, GoalRetention retention, LocalDate applyDate) {
        this.pendingDailyGoal = dailyGoal;
        this.pendingGoalRetention = retention;
        this.pendingApplyDate = applyDate;
    }

    /**
     * 예약된 목표 변경 정보를 초기화한다.
     */
    public void clearPendingGoalSetting() {
        this.pendingDailyGoal = null;
        this.pendingGoalRetention = null;
        this.pendingApplyDate = null;
    }

    public void activate() {
        this.status = MemberStatus.ACTIVE;
        this.deletedAt = null;
    }

    public void promoteToUser() {
        if (this.role == Role.ROLE_GUEST) {
            this.role = Role.ROLE_USER;
        }
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void softDelete() {
        this.status = MemberStatus.WITHDRAWN;
        this.deletedAt = LocalDateTime.now();
    }

    public void updateProfile(
            Integer profileImage,
            String name,
            LocalDate birthday,
            Gender gender
    ) {
        this.profileImage = profileImage;
        this.name = name;
        this.birthday = birthday;
        this.gender = gender;
    }
}
