package com.example.moamoa_backend.item.dto;

import jakarta.validation.constraints.NotNull;

public record ItemPurchaseRequestDto(@NotNull Long itemId) {
}
