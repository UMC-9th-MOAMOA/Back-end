package com.example.moamoa_backend.domain.item.dto;

import jakarta.validation.constraints.NotNull;

public record AvatarEquipRequestDto(@NotNull Long itemId) {
}
