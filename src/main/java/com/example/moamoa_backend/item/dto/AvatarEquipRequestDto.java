package com.example.moamoa_backend.item.dto;

import jakarta.validation.constraints.NotNull;

public record AvatarEquipRequestDto(@NotNull Long itemId) {
}
