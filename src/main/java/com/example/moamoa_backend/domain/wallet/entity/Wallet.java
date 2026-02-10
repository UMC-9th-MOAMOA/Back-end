package com.example.moamoa_backend.domain.wallet.entity;

import com.example.moamoa_backend.global.entity.BaseEntity;
import com.example.moamoa_backend.domain.member.entity.Member;
import com.example.moamoa_backend.domain.wallet.exception.WalletException;
import com.example.moamoa_backend.domain.wallet.exception.code.WalletErrorCode;

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
		if (amount <= 0) {
			throw new WalletException(WalletErrorCode.INVALID_AMOUNT);
		}
		this.point += amount;
	}

	public void usePoint(int amount) {
		if (amount <= 0) {
			throw new WalletException(WalletErrorCode.INVALID_AMOUNT);
		}
		if (this.point < amount) {
			int shortfall = amount - this.point; //  부족분
			throw new WalletException(WalletErrorCode.INSUFFICIENT_POINTS, shortfall); // 예외에 부족분 포함
		}
		this.point -= amount;
	}
}