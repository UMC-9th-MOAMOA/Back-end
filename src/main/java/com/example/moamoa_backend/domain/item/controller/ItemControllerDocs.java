package com.example.moamoa_backend.domain.item.controller;

import com.example.moamoa_backend.domain.item.dto.AvatarEquipRequestDto;
import com.example.moamoa_backend.domain.item.dto.AvatarEquipmentResponseDto;
import com.example.moamoa_backend.domain.item.dto.ItemPurchaseRequestDto;
import com.example.moamoa_backend.domain.item.dto.ItemPurchaseResponseDto;
import com.example.moamoa_backend.domain.item.dto.ItemShopListResponseDto;
import com.example.moamoa_backend.domain.item.enums.ItemCategory;
import com.example.moamoa_backend.domain.item.enums.ItemType;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Item API", description = "아이템 상점/구매/착장 관련 API")
public interface ItemControllerDocs {

	@Operation(
		summary = "카테고리별 아이템 목록 조회",
		description = """
			카테고리(예: MISC)와 (선택)서브타입(type)으로 상점 아이템 목록을 조회합니다.<br><br>
			
			**[인증 필요]**<br>
			Authorization: Bearer {accessToken}<br><br>
			
			**[Query Params]**<br>
			• category (필수): 아이템 카테고리<br>
			• type (선택): 아이템 서브타입(필요한 경우만 전달)<br><br>
			
			**[응답]**<br>
			• 상점 아이템 목록을 반환합니다.<br>
			• 아이템 기본 정보(가격/이미지/카테고리/타입 등) + 사용자 기준 상태(보유 여부 등)를 포함합니다.
			"""
	)
	ApiResponse<ItemShopListResponseDto> getItems(
		@AuthenticationPrincipal UserDetails userDetails,
		@RequestParam ItemCategory category,
		@RequestParam(required = false) ItemType type
	);

	@Operation(
		summary = "아이템 구매",
		description = """
			아이템을 구매하고 도토리를 차감합니다.<br><br>
			
			**[인증 필요]**<br>
			Authorization: Bearer {accessToken}<br><br>
			
			**[Request Body]**<br>
			• itemId (필수): 구매할 아이템 ID<br><br>
			
			**[동작 방식]**<br>
			• 구매 성공 시: 아이템 보유 처리 + 도토리 차감<br>
			• 구매 실패 시: 잔액 부족/중복 구매/유효하지 않은 아이템 등 정책에 따라 실패 응답 반환<br><br>
			
			**[응답]**<br>
			• 구매 결과(구매 아이템/차감 이후 도토리 등)를 반환합니다.
			"""
	)
	ApiResponse<ItemPurchaseResponseDto> purchaseItem(
		@AuthenticationPrincipal UserDetails userDetails,
		@Valid @RequestBody ItemPurchaseRequestDto request
	);

	@Operation(
		summary = "아바타 착장 변경",
		description = """
			보유한 아이템으로 아바타 착장을 변경합니다.<br><br>
			
			**[인증 필요]**<br>
			Authorization: Bearer {accessToken}<br><br>
			
			**[Request Body]**<br>
			• itemId (필수): 장착/해제할 아이템 ID<br><br>
			
			**[동작 방식]**<br>
			• 미착용 아이템: 장착 처리<br>
			• 이미 착용 중인 아이템: 장착 해제 처리(토글)<br>
			• 슬롯을 공유하는 아이템(예: 같은 부위)은 기존 장착이 해제되고 신규 장착으로 교체될 수 있습니다(정책에 따름).<br><br>
			
			**[응답]**<br>
			• 변경된 착장(슬롯별 장착 상태)을 반환합니다.
			"""
	)
	ApiResponse<AvatarEquipmentResponseDto> equip(
		@AuthenticationPrincipal UserDetails userDetails,
		@Valid @RequestBody AvatarEquipRequestDto request
	);
}
