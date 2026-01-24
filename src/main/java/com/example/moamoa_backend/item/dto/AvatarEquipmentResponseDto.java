package com.example.moamoa_backend.item.dto;

import com.example.moamoa_backend.item.enums.ItemType;

import java.util.List;

public record AvatarEquipmentResponseDto(List<EquippedItem> equippedItems) {
	public record EquippedItem(ItemType type, Long itemId, String imageUrl) {
	}
}
