package com.example.moamoa_backend.domain.member.entity.mapping;

import com.example.moamoa_backend.global.entity.BaseEntity;
import com.example.moamoa_backend.domain.interest.entity.SubInterest;
import com.example.moamoa_backend.domain.member.entity.Member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
	@UniqueConstraint(
		name = "uk_member_interest",
		columnNames = {"member_id", "sub_interest_id"}
	)
})
public class MemberSubInterest extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sub_interest_id", nullable = false)
	private SubInterest subInterest;

	public static MemberSubInterest of(Member member, SubInterest subInterest) {
		MemberSubInterest entity = new MemberSubInterest();
		entity.member = member;
		entity.subInterest = subInterest;
		return entity;
	}
}
