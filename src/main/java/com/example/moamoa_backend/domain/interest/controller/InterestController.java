package com.example.moamoa_backend.domain.interest.controller;

import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.domain.interest.dto.InterestResponseDto;
import com.example.moamoa_backend.domain.interest.dto.SubInterestResponseDto;
import com.example.moamoa_backend.domain.interest.exception.code.InterestSuccessCode;
import com.example.moamoa_backend.domain.interest.service.InterestService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/interests")
public class InterestController implements InterestControllerDocs {

	private final InterestService interestService;

	@Override
	@GetMapping
	public ApiResponse<List<InterestResponseDto>> getInterests() {
		List<InterestResponseDto> result = interestService.getInterests();
		return ApiResponse.onSuccess(InterestSuccessCode.INTEREST_LIST_OK, result);
	}

	@Override
	@GetMapping("/{interestId}/details")
	public ApiResponse<List<SubInterestResponseDto>> getSubInterests(
		@PathVariable Long interestId
	) {
		List<SubInterestResponseDto> result = interestService.getSubInterests(interestId);
		return ApiResponse.onSuccess(InterestSuccessCode.SUB_INTEREST_LIST_OK, result);
	}
}
