package com.example.moamoa_backend.member.entity;

import java.time.LocalDate;

import com.example.moamoa_backend.global.entity.BaseEntity;
import com.example.moamoa_backend.member.enums.GoalResultStatus;
import com.example.moamoa_backend.member.enums.GoalResultType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "goal_result",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_goal_result_member_type_date",
		columnNames = {"member_id", "goalType", "goalDate"}
	)
)
public class GoalResult extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private GoalResultType goalType;

	@Column(nullable = false)
	private LocalDate goalDate;

	@Column(nullable = false)
	private Integer targetCount;

	@Column(nullable = false)
	private Integer achievedCount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private GoalResultStatus status;

	private GoalResult(
		Member member,
		GoalResultType goalType,
		LocalDate goalDate,
		int targetCount,
		int achievedCount,
		GoalResultStatus status
	) {
		this.member = member;
		this.goalType = goalType;
		this.goalDate = goalDate;
		this.targetCount = targetCount;
		this.achievedCount = achievedCount;
		this.status = status;
	}

	public static GoalResult createDaily(
		Member member,
		LocalDate goalDate,
		int targetCount,
		int achievedCount,
		GoalResultStatus status
	) {
		return new GoalResult(member, GoalResultType.DAILY, goalDate, targetCount, achievedCount, status);
	}

	public static GoalResult createWeekly(
		Member member,
		LocalDate goalDate,
		int targetCount,
		int achievedCount,
		GoalResultStatus status
	) {
		return new GoalResult(member, GoalResultType.WEEKLY, goalDate, targetCount, achievedCount, status);
	}
}
