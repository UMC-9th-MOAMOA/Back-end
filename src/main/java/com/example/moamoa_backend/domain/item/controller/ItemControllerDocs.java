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
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

public interface ItemControllerDocs {
	@Operation(summary = "카테고리별 아이템 목록 조회", description = "카테고리(예: MISC)와 (선택)서브타입(type)으로 상점 목록을 조회합니다.")
	public ApiResponse<ItemShopListResponseDto> getItems(
		@AuthenticationPrincipal UserDetails userDetails,
		@RequestParam ItemCategory category,
		@RequestParam(required = false) ItemType type
	) ;

	@Operation(summary = "아이템 구매", description = "아이템을 구매하고 도토리를 차감합니다.")
	public ApiResponse<ItemPurchaseResponseDto> purchaseItem(
		@AuthenticationPrincipal UserDetails userDetails,
		@Valid @RequestBody ItemPurchaseRequestDto request
	) ;

	@Operation(summary = "아바타 착장 변경", description = "보유한 아이템으로 아바타 착장을 변경합니다.")
	public ApiResponse<AvatarEquipmentResponseDto> equip(
		@AuthenticationPrincipal UserDetails userDetails,
		@Valid @RequestBody AvatarEquipRequestDto request
	) ;

}
