package com.example.moamoa_backend.item.dto;

import com.example.moamoa_backend.item.enums.ItemType;

import java.util.Map;

public record HomeResponseDto(
	String memberName,
	Integer point,
	boolean shouldShowTutorial,
	Map<ItemType, EquippedItem> equippedItems
) {
	public record EquippedItem(Long itemId, String name, String imageUrl) {
		public static EquippedItem from(Long itemId, String name, String imageUrl) {
			return new EquippedItem(itemId, name, imageUrl);
		}
	}

	public static HomeResponseDto of(
		String memberName,
		Integer point,
		boolean shouldShowTutorial,
		Map<ItemType, EquippedItem> equippedItems
	) {
		return new HomeResponseDto(memberName, point, shouldShowTutorial, equippedItems);
	}
}
