package com.example.moamoa_backend.item.service;

import com.example.moamoa_backend.item.dto.AvatarEquipmentResponseDto;
import com.example.moamoa_backend.item.dto.ItemPurchaseResponseDto;
import com.example.moamoa_backend.item.dto.ItemShopListResponseDto;
import com.example.moamoa_backend.item.entity.Item;
import com.example.moamoa_backend.item.entity.MemberItem;
import com.example.moamoa_backend.item.enums.EquipSlot;
import com.example.moamoa_backend.item.enums.ItemCategory;
import com.example.moamoa_backend.item.enums.ItemType;
import com.example.moamoa_backend.item.exception.ItemException;
import com.example.moamoa_backend.item.exception.code.ItemErrorCode;
import com.example.moamoa_backend.item.repository.ItemRepository;
import com.example.moamoa_backend.item.repository.MemberItemRepository;
import com.example.moamoa_backend.member.entity.Member;
import com.example.moamoa_backend.member.exception.MemberException;
import com.example.moamoa_backend.member.exception.code.MemberErrorCode;
import com.example.moamoa_backend.member.repository.MemberRepository;
import com.example.moamoa_backend.wallet.entity.Wallet;
import com.example.moamoa_backend.wallet.entity.WalletHistory;
import com.example.moamoa_backend.wallet.exception.WalletException;
import com.example.moamoa_backend.wallet.exception.code.WalletErrorCode;
import com.example.moamoa_backend.wallet.repository.WalletHistoryRepository;
import com.example.moamoa_backend.wallet.repository.WalletRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemShopService {

	private final ItemRepository itemRepository;
	private final MemberItemRepository memberItemRepository;
	private final MemberRepository memberRepository;
	private final WalletRepository walletRepository;
	private final WalletHistoryRepository walletHistoryRepository;

    /**
     * 상점 목록 조회
     * - category: 상위 카테고리(FACE/TOP/BOTTOM/MISC/BACKGROUND)
     * - type(선택): 카테고리 내부 서브타입(HAT/GLASSES/...)
     */
	public ItemShopListResponseDto getShopItems(Long memberId, ItemCategory category, ItemType type) {
		Wallet wallet = walletRepository.findByMemberId(memberId)
			.orElseThrow(() -> new WalletException(WalletErrorCode.WALLET_NOT_FOUND));

		List<ItemType> candidateTypes = category.types();

        // (선택) type 필터가 들어오면 category 소속인지 검증
		List<ItemType> queryTypes = candidateTypes;
		if (type != null) {
			if (!candidateTypes.contains(type)) {
				throw new ItemException(ItemErrorCode.ITEM_INVALID_CATEGORY_TYPE);
			}
			queryTypes = List.of(type);
		}
		List<Item> items = itemRepository.findByTypeIn(queryTypes);

		// EntityGraph로 item까지 함께 로딩 (ownedMap 만들 때 N+1 방지)
		List<MemberItem> owned = memberItemRepository.findByMemberIdAndItem_TypeIn(memberId, queryTypes);

		Map<Long, MemberItem> ownedMap = owned.stream()
			.collect(Collectors.toMap(
				mi -> mi.getItem().getId(),
				mi -> mi,
				(a, b) -> a
			));

		int walletPoint = wallet.getPoint();

		List<ItemShopListResponseDto.ItemShopItemResponseDto> responseItems = items.stream()
			.map(item -> {
				MemberItem memberItem = ownedMap.get(item.getId());
				boolean ownedFlag = memberItem != null;
				boolean equippedFlag = ownedFlag && memberItem.isEquipped();
				boolean affordableFlag = item.isOnSale() && !ownedFlag && walletPoint >= item.getPrice();

				return new ItemShopListResponseDto.ItemShopItemResponseDto(
					item.getId(),
					category, //상위 카테고리
					item.getType(), //서브 타입
					item.getName(),
					item.getPrice(),
					item.getImageUrl(),
					item.isOnSale(),
					ownedFlag,
					equippedFlag,
					affordableFlag
				);
			})
			.toList();

		return new ItemShopListResponseDto(type, walletPoint, responseItems);
	}


    /**
     * 아이템 구매
     */
	@Transactional
	public ItemPurchaseResponseDto purchase(Long memberId, Long itemId) {
		Item item = itemRepository.findById(itemId)
			.orElseThrow(() -> new ItemException(ItemErrorCode.ITEM_NOT_FOUND));

		if (!item.isOnSale()) {
			throw new ItemException(ItemErrorCode.ITEM_NOT_ON_SALE);
		}

		// (동시성 방지) 동일 member의 지갑 row를 락으로 선점
		Wallet wallet = walletRepository.findByMemberIdForUpdate(memberId)
			.orElseThrow(() -> new WalletException(WalletErrorCode.WALLET_NOT_FOUND));

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

		// 검증 + 차감은 Wallet 도메인이 책임(부족하면 WalletException 발생)
		wallet.usePoint(item.getPrice());

		try {
			// (중복 구매 최종 방어) uk_member_item 위반을 try/catch 내부에서 확정적으로 감지하기 위해 flush 수행
			memberItemRepository.saveAndFlush(MemberItem.create(member, item));
		} catch (DataIntegrityViolationException e) {
			/**
			 * uk_member_item(unique member_id + item_id) 위반 등으로 중복 구매가 확정된 경우.
			 * @Transactional 이므로 예외가 던져지면 전체 롤백되어
			 * wallet.usePoint()로 차감된 point도 함께 롤백된다.
			 */
			throw new ItemException(ItemErrorCode.ITEM_ALREADY_OWNED);
		}

		// 구매 히스토리 기록(정책: 구매는 amount 음수)
		walletHistoryRepository.save(
			WalletHistory.forPurchase(
				wallet,
				item,                  // ✅ 추가: FK 연결
				item.getPrice(),
				wallet.getPoint(),
				"아이템 구매: " + item.getName()
			)
		);

		return new ItemPurchaseResponseDto(item.getId(), item.getPrice(), wallet.getPoint());
	}

    /**
     * 아바타 착장 변경
     * - 같은 EquipSlot(=잡화면 MISC)인 기존 착장만 해제
     * - 잡화는 1개만 착용 가능
     */
	@Transactional
	public AvatarEquipmentResponseDto equip(Long memberId, Long itemId) {

		memberRepository.findByIdForUpdate(memberId)
			.orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

		MemberItem target = memberItemRepository.findByMemberIdAndItemId(memberId, itemId)
			.orElseThrow(() -> new ItemException(ItemErrorCode.ITEM_NOT_OWNED));

        EquipSlot targetSlot = target.getItem().getType().slot();

        // 현재 착용중 전체 조회 후, 같은 슬롯인 것만 해제
        List<MemberItem> equippedAll = memberItemRepository.findByMemberIdAndIsEquippedTrue(memberId);

        for (MemberItem mi : equippedAll) {
            if (mi.getItem().getType().slot() == targetSlot) {
                mi.unequip();
            }
        }

		target.equip();

        // 응답용: 착용 전체 다시 내려주기
        List<MemberItem> equippedAfter = memberItemRepository.findByMemberIdAndIsEquippedTrue(memberId);

        List<AvatarEquipmentResponseDto.EquippedItem> equippedDtos = equippedAfter.stream()
            .map(mi -> new AvatarEquipmentResponseDto.EquippedItem(
                mi.getItem().getType(),
                mi.getItem().getId(),
                mi.getItem().getImageUrl()
            ))
            .toList();

        return new AvatarEquipmentResponseDto(equippedDtos);
    }
}
