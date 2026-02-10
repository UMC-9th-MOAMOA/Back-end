package com.example.moamoa_backend.domain.item.enums;

public enum ItemType {
    FACE,

    TOP,
    BOTTOM,

    // 잡화 서브타입들
    HAT,
    GLASSES,
    SCARF,
    SHOES,
    GLOVES,

    BACKGROUND;

    public EquipSlot slot() {
        return switch (this) {
            case FACE -> EquipSlot.FACE;
            case TOP -> EquipSlot.TOP;
            case BOTTOM -> EquipSlot.BOTTOM;

            // 잡화는 1개만 착용 가능
            case HAT, GLASSES, SCARF, SHOES, GLOVES -> EquipSlot.MISC;

            case BACKGROUND -> EquipSlot.BACKGROUND;
        };
    }
}
