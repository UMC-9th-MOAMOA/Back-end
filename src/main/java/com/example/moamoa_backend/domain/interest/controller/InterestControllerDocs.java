package com.example.moamoa_backend.domain.interest.controller;

import java.util.List;

import com.example.moamoa_backend.domain.interest.dto.InterestResponseDto;
import com.example.moamoa_backend.domain.interest.dto.SubInterestResponseDto;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Interest API", description = "온보딩 관심사(대분류/세부분류) 조회 API")
public interface InterestControllerDocs {

	@Operation(
		summary = "대분류 관심사 목록 조회",
		description = """
			온보딩에서 선택 가능한 **대분류 관심사** 목록을 조회합니다.<br><br>
			
			**[인증]**<br>
			• 인증 없이 호출 가능합니다.<br><br>
			
			**[응답]**<br>
			• 대분류 관심사 목록을 반환합니다.
			"""
	)
	ApiResponse<List<InterestResponseDto>> getInterests();

	@Operation(
		summary = "세부 관심사 목록 조회",
		description = """
			interestId에 해당하는 **세부 관심사** 목록을 조회합니다.<br><br>
			
			**[Path Variable]**<br>
			• interestId (필수): 대분류 관심사 ID<br><br>
			
			**[응답]**<br>
			• 해당 대분류의 세부 관심사 목록을 반환합니다.
			"""
	)
	ApiResponse<List<SubInterestResponseDto>> getSubInterests(
		@PathVariable Long interestId
	);
}
