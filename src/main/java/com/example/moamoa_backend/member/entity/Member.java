package com.example.moamoa_backend.member.entity;

import com.example.moamoa_backend.global.entity.BaseEntity;
import com.example.moamoa_backend.member.enums.*;
import com.example.moamoa_backend.member.exception.MemberException;
import com.example.moamoa_backend.member.exception.code.MemberErrorCode;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
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

	@Column(nullable = false)
	@Builder.Default
	private Boolean policyAgreed = false;

	/**
	 * 온보딩 완료 처리
	 */
	public void completeOnboarding() {
		if (Boolean.TRUE.equals(this.onboardingCompleted))
			return; // 이미 완료면 스킵
		this.onboardingCompleted = true;
	}

	/**
	 * 일간 목표는 언제든 "즉시" 적용한다.
	 */
	public void applyDailyGoalNow(Integer dailyGoal) {
		if (dailyGoal == null) {
			turnOffGoals();
			return;
		}
		this.dailyGoal = dailyGoal;
	}

	/**
	 * 주간 목표는 "월~일" 기준이므로, 주간 목표 값 갱신은 반드시 월요일 startDate로만 한다.
	 * weeklyGoal = dailyGoal * 5 (평일 5일 기준)
	 */
	public void applyWeeklyGoalNow(Integer baseDailyGoal, GoalRetention retention, LocalDate startDateMonday) {
		if (baseDailyGoal == null) {
			this.weeklyGoal = null;
			this.goalRetention = null;
			this.goalEndDate = null;
			return;
		}
		if (startDateMonday == null) {
			throw new MemberException(MemberErrorCode.GOAL_APPLY_DATE_REQUIRED);
		}
		if (startDateMonday.getDayOfWeek() != DayOfWeek.MONDAY) {
			throw new MemberException(MemberErrorCode.GOAL_APPLY_DATE_MUST_BE_MONDAY);
		}
		this.weeklyGoal = baseDailyGoal * 5;
		this.goalRetention = retention;
		this.goalEndDate = retention == null ? null : retention.calculateEndDate(startDateMonday);
	}

	/**
	 * 목표 OFF: 일간/주간/유지기간/예약 모두 제거
	 */
	public void turnOffGoals() {
		this.dailyGoal = null;
		this.weeklyGoal = null;
		this.goalRetention = null;
		this.goalEndDate = null;
		clearPendingGoalSetting();
	}

	/**
	 * 목표 변경을 다음 주 적용으로 예약한다.
	 */
	public void scheduleGoalSetting(Integer dailyGoal, GoalRetention retention, LocalDate applyDate) {

		// applyDate는 "월요일"이어야 한다
		if (applyDate == null) {
			throw new MemberException(MemberErrorCode.GOAL_APPLY_DATE_REQUIRED);
		}
		if (applyDate.getDayOfWeek() != DayOfWeek.MONDAY) {
			throw new MemberException(MemberErrorCode.GOAL_APPLY_DATE_MUST_BE_MONDAY);
		}

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

	public void completePolicyAgreement() {
		this.policyAgreed = true;
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

	/**
	 * (레거시) 월요일에만 전체 목표(daily+weekly)를 한 번에 적용할 때 사용.
	 * startDate는 반드시 월요일이어야 한다.
	 */
	@Deprecated(forRemoval = true)
	public void applyGoalSetting(Integer dailyGoal, GoalRetention retention, LocalDate startDate) {

		// (기존 메서드는 호환 유지: "월요일에 전체 적용" 용도로만 사용)
		if (dailyGoal == null) {
			turnOffGoals();
			return;
		}
		applyDailyGoalNow(dailyGoal);
		applyWeeklyGoalNow(dailyGoal, retention, startDate);
	}
}
