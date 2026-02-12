package com.example.moamoa_backend.domain.mission.controller;

import com.example.moamoa_backend.global.apiPayload.code.GeneralSuccessCode;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.domain.mission.dto.request.MissionRequestDto;
import com.example.moamoa_backend.domain.mission.dto.response.MissionResponseDto;
import com.example.moamoa_backend.domain.mission.service.command.MissionCommandService;
import com.example.moamoa_backend.domain.mission.service.query.MissionQueryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/missions")
public class MissionController implements MissionControllerDocs {

	private final MissionCommandService missionCommandService;
	private final MissionQueryService missionQueryService;

	@Override
	@PostMapping("/admin")
	public ApiResponse<MissionResponseDto.CreateResult> createMission(
		@RequestBody @Valid MissionRequestDto.Create request
	) {
		MissionResponseDto.CreateResult result = missionCommandService.createMission(request);

		return ApiResponse.onSuccess(GeneralSuccessCode.CREATED, result);
	}

	@Override
	@GetMapping("/{missionId}")
	public ApiResponse<MissionResponseDto.MissionDetail> getMissionDetail(
		@PathVariable Long missionId,
		@AuthenticationPrincipal UserDetails userDetails
	) {
		Long memberId = Long.parseLong(userDetails.getUsername());

		MissionResponseDto.MissionDetail result = missionQueryService.getMissionDetail(memberId, missionId);

		return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
	}

	@Override
	@PostMapping("/{missionId}/watch")
	public ApiResponse<MissionResponseDto.WatchResult> updateWatchStatus(
		@PathVariable Long missionId,
		@AuthenticationPrincipal UserDetails userDetails
	) {
		Long memberId = Long.parseLong(userDetails.getUsername());
		MissionResponseDto.WatchResult result = missionCommandService.updateMissionWatchStatus(memberId, missionId);

		return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
	}

	@Override
	@PatchMapping("/{missionId}/status")
	public ApiResponse<MissionResponseDto.StatusResult> updateStatus(
		@PathVariable Long missionId,
		@Valid @RequestBody MissionRequestDto.PatchStatus request,
		@AuthenticationPrincipal UserDetails userDetails
	) {
		Long memberId = Long.parseLong(userDetails.getUsername());
		MissionResponseDto.StatusResult result = missionCommandService.updateMissionStatus(memberId, missionId,
			request);

		return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);

	}

	@Override
	@GetMapping("/keywords")
	public ApiResponse<MissionResponseDto.KeywordListResult> getRecommendKeyword() {
		MissionResponseDto.KeywordListResult result = missionQueryService.getRecommendedKeywords();
		return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
	}

	@Override
	@GetMapping("/keywords/related")
	public ApiResponse<MissionResponseDto.KeywordListResult> getRelatedKeywords(
		@RequestParam String keyword
	) {
		MissionResponseDto.KeywordListResult result = missionQueryService.getRelatedKeywords(keyword);
		return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
	}

	@Override
	@GetMapping("/recommend")
	public ApiResponse<List<MissionResponseDto.RecommendResult>> getTodayRecommendMissions(
		@RequestParam(required = false) Integer time,
        @RequestParam(required = false, defaultValue = "false") Boolean isRefresh,
		@AuthenticationPrincipal UserDetails userDetails
	) {
		Long memberId = Long.parseLong(userDetails.getUsername());
		List<MissionResponseDto.RecommendResult> result = missionQueryService.getTodayRecommendMissions(memberId, time);

		return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
	}

	@Override
	@GetMapping("/search")
	public ApiResponse<MissionResponseDto.SearchResponse> searchMissions(
		@AuthenticationPrincipal UserDetails userDetails,
		@RequestParam(required = false) String searchText,
		@RequestParam(required = false) List<String> keywords,
		@RequestParam(required = false) Long seed,
		@PageableDefault(size = 10) Pageable pageable
	) {
		Long memberId = Long.parseLong(userDetails.getUsername());

		Long searchSeed = (seed != null) ? seed : System.currentTimeMillis();

		return ApiResponse.onSuccess(GeneralSuccessCode.OK,
			missionQueryService.searchMissions(memberId, searchText, keywords, searchSeed, pageable)
		);
	}

	@Override
	@GetMapping("/categories")
	public ApiResponse<MissionResponseDto.SearchResponse> getMissionsByCategory(
		@AuthenticationPrincipal UserDetails userDetails,
		@RequestParam(required = false) Long categoryId,
		@RequestParam(required = false) Long subCategoryId,
		@RequestParam(required = false) Long seed,
		@PageableDefault(size = 10) Pageable pageable
	) {
		Long memberId = Long.parseLong(userDetails.getUsername());
		Long searchSeed = (seed != null) ? seed : System.currentTimeMillis();

		return ApiResponse.onSuccess(
			GeneralSuccessCode.OK,
			missionQueryService.getMissionsByCategory(memberId, categoryId, subCategoryId, searchSeed, pageable)
		);
	}

	@Override
	@GetMapping("/me")
	public ApiResponse<MissionResponseDto.SearchResponse> getMyMissions(
		@AuthenticationPrincipal UserDetails userDetails,
		@RequestParam String status,
		@RequestParam(required = false) String condition,
		@RequestParam(required = false) Long categoryId,
		@PageableDefault(size = 10) Pageable pageable
	) {
		Long memberId = Long.parseLong(userDetails.getUsername());

		return ApiResponse.onSuccess(
			GeneralSuccessCode.OK,
			missionQueryService.getMyMissions(memberId, status, condition, categoryId, pageable)
		);
	}

	@Override
	@PostMapping("/{missionId}/submit")
	public ApiResponse<MissionResponseDto.SubmitResult> submitMission(
		@AuthenticationPrincipal UserDetails userDetails,
		@PathVariable Long missionId,
		@RequestBody @Valid MissionRequestDto.SubmitAnswer request
	) {
		Long memberId = Long.parseLong(userDetails.getUsername());
		return ApiResponse.onSuccess(
			GeneralSuccessCode.OK,
			missionCommandService.submitMissionAnswer(memberId, missionId, request)
		);
	}
}