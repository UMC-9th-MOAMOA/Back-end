package com.example.moamoa_backend.domain.interest.controller;

import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.domain.interest.dto.InterestResponseDto;
import com.example.moamoa_backend.domain.interest.dto.SubInterestResponseDto;
import com.example.moamoa_backend.domain.interest.exception.code.InterestSuccessCode;
import com.example.moamoa_backend.domain.interest.service.InterestService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

public interface InterestControllerDocs {
	@Operation(summary = "대분류 관심사 목록 조회", description = "온보딩에서 선택 가능한 대분류 관심사 목록을 조회합니다.")
	public ApiResponse<List<InterestResponseDto>> getInterests() ;

	@Operation(summary = "세부 관심사 목록 조회", description = "interestId에 해당하는 세부 관심사 목록을 조회합니다.")
	public ApiResponse<List<SubInterestResponseDto>> getSubInterests(
		@PathVariable Long interestId
	) ;

}
