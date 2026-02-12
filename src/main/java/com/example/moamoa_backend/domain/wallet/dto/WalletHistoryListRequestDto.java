package com.example.moamoa_backend.domain.wallet.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class WalletHistoryListRequestDto {
	@Getter
	@RequiredArgsConstructor
	public enum Tab {
		ALL("전체"),
		EARN("적립"),
		USE("사용");

		private final String description;
	}

	@Getter
	@RequiredArgsConstructor
	public enum Sort {
		RECENT("최근순"),
		OLDEST("오래된순");

		private final String description;
	}

	@Getter
	@RequiredArgsConstructor
	public enum Period {
		ALL("전체"),
		THREE_MONTHS("3개월"),
		SIX_MONTHS("6개월");

		private final String description;
	}

	@Getter
	@RequiredArgsConstructor
	public enum EarnSource {
		ALL("전체"),
		MISSION("미션"),
		ATTENDANCE("출석");

		private final String description;
	}
}
