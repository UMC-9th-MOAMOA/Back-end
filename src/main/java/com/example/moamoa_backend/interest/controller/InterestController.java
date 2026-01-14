package com.example.moamoa_backend.interest.controller;

import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.interest.dto.InterestResponse;
import com.example.moamoa_backend.interest.dto.SubInterestResponse;
import com.example.moamoa_backend.interest.exception.code.InterestSuccessCode;
import com.example.moamoa_backend.interest.service.InterestService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/interests")
public class InterestController {

	private final InterestService interestService;

	@Operation(summary = "대분류 관심사 목록 조회", description = "온보딩에서 선택 가능한 대분류 관심사 목록을 조회합니다.")
	@GetMapping
	public ResponseEntity<ApiResponse<List<InterestResponse>>> getInterests() {
		List<InterestResponse> result = interestService.getInterests();
		return ResponseEntity
			.status(InterestSuccessCode.INTEREST_LIST_OK.getStatus())
			.body(ApiResponse.onSuccess(InterestSuccessCode.INTEREST_LIST_OK, result));
	}

	@Operation(summary = "세부 관심사 목록 조회", description = "interestId에 해당하는 세부 관심사 목록을 조회합니다.")
	@GetMapping("/{interestId}/details")
	public ResponseEntity<ApiResponse<List<SubInterestResponse>>> getSubInterests(
		@PathVariable Long interestId
	) {
		List<SubInterestResponse> result = interestService.getSubInterests(interestId);
		return ResponseEntity
			.status(InterestSuccessCode.SUB_INTEREST_LIST_OK.getStatus())
			.body(ApiResponse.onSuccess(InterestSuccessCode.SUB_INTEREST_LIST_OK, result));
	}
}
