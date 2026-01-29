package com.example.moamoa_backend.item.service;

import com.example.moamoa_backend.item.dto.HomeResponseDto;
import com.example.moamoa_backend.item.entity.MemberItem;
import com.example.moamoa_backend.item.enums.ItemType;
import com.example.moamoa_backend.item.repository.MemberItemRepository;
import com.example.moamoa_backend.member.entity.Member;
import com.example.moamoa_backend.member.exception.MemberException;
import com.example.moamoa_backend.member.exception.code.MemberErrorCode;
import com.example.moamoa_backend.member.repository.MemberRepository;
import com.example.moamoa_backend.wallet.entity.Wallet;
import com.example.moamoa_backend.wallet.exception.WalletException;
import com.example.moamoa_backend.wallet.exception.code.WalletErrorCode;
import com.example.moamoa_backend.wallet.repository.WalletRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeService {

	private final MemberRepository memberRepository;
	private final MemberItemRepository memberItemRepository;
	private final WalletRepository walletRepository;

	@Transactional(readOnly = true)
	public HomeResponseDto getHome(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

		Wallet wallet = walletRepository.findByMemberId(memberId)
			.orElseThrow(() -> new WalletException(WalletErrorCode.WALLET_NOT_FOUND));

		int point = wallet.getPoint();

		// EntityGraph로 item까지 함께 로딩 (N+1 방지)
		List<MemberItem> equippedAll = memberItemRepository.findByMemberIdAndIsEquippedTrue(memberId);

		Map<ItemType, HomeResponseDto.EquippedItem> equippedItems = equippedAll.stream()
			.collect(Collectors.toMap(
				mi -> mi.getItem().getType(),
				mi -> HomeResponseDto.EquippedItem.from(
					mi.getItem().getId(),
					mi.getItem().getName(),
					mi.getItem().getImageUrl()
				),
				(a, b) -> a, // 동일 타입이 중복 장착된 데이터가 있으면 첫 번째 우선
				() -> new EnumMap<>(ItemType.class)
			));

		return HomeResponseDto.of(member.getName(), point, equippedItems);
	}
}
