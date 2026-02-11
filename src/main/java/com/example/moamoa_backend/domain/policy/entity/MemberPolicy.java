package com.example.moamoa_backend.domain.policy.entity;

import com.example.moamoa_backend.global.entity.BaseEntity;
import com.example.moamoa_backend.domain.member.entity.Member;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 회원-약관 동의 매핑 엔티티
 * - 회원별 약관 동의 여부 및 동의 시점 관리
 * - 회원(Member)과 약관(Policy)의 다대다 관계를 중간 테이블로 해소
 */
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

	/**
	 * 약관 동의 상태 변경
	 * @param isAgreed 동의 여부 (true: 동의, false: 철회)
	 * TODO: 추후 동의/철회 이력 관리 예정 -> MemberPolicyHistory 테이블 분리 고려 중
	 */
	public void updateAgreement(boolean isAgreed) {
		this.isAgreed = isAgreed;
		this.agreedAt = isAgreed ? LocalDateTime.now() : null;
	}
}
