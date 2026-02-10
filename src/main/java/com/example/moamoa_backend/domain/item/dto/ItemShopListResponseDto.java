package com.example.moamoa_backend.domain.item.dto;

import com.example.moamoa_backend.domain.item.enums.ItemCategory;
import com.example.moamoa_backend.domain.item.enums.ItemType;

import java.util.List;

public record ItemShopListResponseDto(
	ItemType type,  // optional filter(없으면 null)
	Integer walletPoint,
	List<ItemShopItemResponseDto> items
) {
	public record ItemShopItemResponseDto(
		Long itemId,
		ItemCategory category, //  잡화인지
		ItemType type,         //  안경/모자/장갑/신발 등 서브타입
		String name,
		Integer price,
		String imageUrl,
		boolean onSale,
		boolean owned,
		boolean equipped,
		boolean affordable
	) {}
}
