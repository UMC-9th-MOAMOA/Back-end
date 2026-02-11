package com.example.moamoa_backend.domain.item.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.domain.item.dto.AvatarEquipRequestDto;
import com.example.moamoa_backend.domain.item.dto.AvatarEquipmentResponseDto;
import com.example.moamoa_backend.domain.item.dto.ItemPurchaseRequestDto;
import com.example.moamoa_backend.domain.item.dto.ItemPurchaseResponseDto;
import com.example.moamoa_backend.domain.item.dto.ItemShopListResponseDto;
import com.example.moamoa_backend.domain.item.enums.ItemCategory;
import com.example.moamoa_backend.domain.item.enums.ItemType;
import com.example.moamoa_backend.domain.item.exception.code.ItemSuccessCode;
import com.example.moamoa_backend.domain.item.service.ItemShopService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ItemController implements ItemControllerDocs {

	private final ItemShopService itemShopService;

	@Override
	@GetMapping("/items")
	public ApiResponse<ItemShopListResponseDto> getItems(
		@AuthenticationPrincipal UserDetails userDetails,
		@RequestParam ItemCategory category,
		@RequestParam(required = false) ItemType type
	) {
		Long memberId = extractMemberId(userDetails);
		ItemShopListResponseDto result = itemShopService.getShopItems(memberId, category, type);
		return ApiResponse.onSuccess(ItemSuccessCode.ITEM_LIST_OK, result);
	}

	@Override
	@PostMapping("/members/me/purchases")
	public ApiResponse<ItemPurchaseResponseDto> purchaseItem(
		@AuthenticationPrincipal UserDetails userDetails,
		@Valid @RequestBody ItemPurchaseRequestDto request
	) {
		Long memberId = extractMemberId(userDetails);
		ItemPurchaseResponseDto result = itemShopService.purchase(memberId, request.itemId());
		return ApiResponse.onSuccess(ItemSuccessCode.ITEM_PURCHASE_OK, result);
	}

	@Override
	@PatchMapping("/members/me/avatar/equipment")
	public ApiResponse<AvatarEquipmentResponseDto> equip(
		@AuthenticationPrincipal UserDetails userDetails,
		@Valid @RequestBody AvatarEquipRequestDto request
	) {
		Long memberId = extractMemberId(userDetails);
		AvatarEquipmentResponseDto result = itemShopService.equip(memberId, request.itemId());
		return ApiResponse.onSuccess(ItemSuccessCode.AVATAR_EQUIP_OK, result);
	}

	private Long extractMemberId(UserDetails userDetails) {
		return Long.parseLong(userDetails.getUsername());
	}
}
