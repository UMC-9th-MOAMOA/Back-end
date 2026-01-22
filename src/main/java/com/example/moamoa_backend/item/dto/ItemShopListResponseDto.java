package com.example.moamoa_backend.item.dto;

import com.example.moamoa_backend.item.enums.ItemType;

import java.util.List;

public record ItemShopListResponseDto(
	ItemType type,
	Integer walletPoint,
	List<ItemShopItemResponseDto> items
) {
	public record ItemShopItemResponseDto(
		Long itemId,
		String name,
		Integer price,
		String imageUrl,
		boolean onSale,
		boolean owned,
		boolean equipped,
		boolean affordable
	) {}
}
