package com.example.moamoa_backend.domain.item.enums;

import java.util.List;

public enum ItemCategory {
	FACE,
	TOP,
	BOTTOM,
	MISC,
	BACKGROUND;

	public List<ItemType> types() {
		return switch (this) {
			case FACE -> List.of(ItemType.FACE);
			case TOP -> List.of(ItemType.TOP);
			case BOTTOM -> List.of(ItemType.BOTTOM);
			case BACKGROUND -> List.of(ItemType.BACKGROUND);

			// 잡화 목록에서 보여줄 서브타입들
			case MISC -> List.of(
				ItemType.HAT,
				ItemType.GLASSES,
				ItemType.SCARF,
				ItemType.SHOES,
				ItemType.GLOVES
			);
		};
	}
}
