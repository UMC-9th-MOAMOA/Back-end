package com.example.moamoa_backend.domain.wallet.entity;

import com.example.moamoa_backend.global.entity.BaseEntity;
import com.example.moamoa_backend.domain.mission.entity.Mission;
import com.example.moamoa_backend.domain.item.entity.Item;
import com.example.moamoa_backend.domain.wallet.enums.TransactionType;
import com.example.moamoa_backend.domain.wallet.exception.WalletException;
import com.example.moamoa_backend.domain.wallet.exception.code.WalletErrorCode;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalletHistory extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * N:1 관계. 지갑 1개에 히스토리는 여러 개.
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "wallet_id", nullable = false)
	private Wallet wallet;

	/**
	 * 사용자에게 보여줄 내역 설명(예: "아이템 구매: 모자")
	 */
	@Column(nullable = false)
	private String description;

	/**
	 * 거래 금액(증가/감소).
	 * 정책: 증가=양수, 감소=음수로 저장하면 합계 계산/통계가 쉬움.
	 */
	@Column(nullable = false)
	private Integer amount;

	/**
	 * 거래 직후 잔액 스냅샷.
	 * 과거 내역을 재구성할 때 정확한 잔액을 보장하기 위해 저장.
	 */
	@Column(nullable = false)
	private Integer balanceSnapshot;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TransactionType type;

	/**
	 * MISSION 타입일 때만 연결되는 미션.
	 * 그 외 타입은 null이어야 한다(도메인 규칙).
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mission_id")
	private Mission mission;

	/**
	 * PURCHASE 타입일 때만 연결되는 아이템
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_id")
	private Item item;

	private WalletHistory(
		Wallet wallet,
		Mission mission,
		Item item,
		String description,
		Integer amount,
		Integer balanceSnapshot,
		TransactionType type
	) {
		validate(type, mission, item);
		this.wallet = wallet;
		this.mission = mission;
		this.item = item;
		this.description = description;
		this.amount = amount;
		this.balanceSnapshot = balanceSnapshot;
		this.type = type;
	}

	/**
	 * 범용 생성 팩토리(정책/규칙은 validate로 강제).
	 */
	public static WalletHistory create(
		Wallet wallet,
		Mission mission,
		Item item,
		String description,
		int amount,
		int balanceSnapshot,
		TransactionType type
	) {
		return new WalletHistory(wallet, mission, item, description, amount, balanceSnapshot, type);
	}

	public static WalletHistory forPurchase(Wallet wallet, Item item, int spent, int balanceSnapshot,
		String description) {
		if (spent <= 0) {
			throw new WalletException(WalletErrorCode.INVALID_AMOUNT);
		}
		return WalletHistory.create(
			wallet,
			null,
			item,
			description,
			-spent,
			balanceSnapshot,
			TransactionType.PURCHASE
		);
	}

	/**
	 * 도메인 규칙:
	 * - type == MISSION 이면 mission 필수
	 * - type != MISSION 이면 mission 금지
	 */
	private static void validate(TransactionType type, Mission mission, Item item) {
		// ✅ 미션 관련 타입은 Mission 연결 필수
		if ((type == TransactionType.MISSION || type == TransactionType.MISSION_COMPLETE) && mission == null) {
			throw new WalletException(WalletErrorCode.MISSION_REQUIRED_FOR_MISSION_TYPE);
		}
		if (!(type == TransactionType.MISSION || type == TransactionType.MISSION_COMPLETE) && mission != null) {
			throw new WalletException(WalletErrorCode.MISSION_NOT_ALLOWED_FOR_NON_MISSION_TYPE);
		}

		// ✅ PURCHASE 타입은 Item 연결 필수
		if (type == TransactionType.PURCHASE && item == null) {
			throw new WalletException(WalletErrorCode.ITEM_REQUIRED_FOR_PURCHASE_TYPE);
		}
		if (type != TransactionType.PURCHASE && item != null) {
			throw new WalletException(WalletErrorCode.ITEM_NOT_ALLOWED_FOR_NON_PURCHASE_TYPE);
		}
	}
}