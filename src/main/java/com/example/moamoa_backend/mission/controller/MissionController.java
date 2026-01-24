package com.example.moamoa_backend.mission.controller;


import com.example.moamoa_backend.global.apiPayload.code.GeneralSuccessCode;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.mission.dto.request.MissionRequestDto;
import com.example.moamoa_backend.mission.dto.response.MissionResponseDto;
import com.example.moamoa_backend.mission.service.command.MissionCommandService;
import com.example.moamoa_backend.mission.service.query.MissionQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name="Mission API", description = "미션 관련 API")
public class MissionController {
    private final MissionCommandService missionCommandService;
    private final MissionQueryService missionQueryService;

    @PostMapping("/admin")
    @Operation(summary = "미션 등록 API(관리자용)", description = "영상 정보와 퀴즈를 입력하면 도토리와 소요시간이 자동 계산되어 등록됩니다.")
    public ApiResponse<MissionResponseDto.CreateResult> createMission(
            @RequestBody @Valid MissionRequestDto.Create request
            ){
        MissionResponseDto.CreateResult result = missionCommandService.createMission(request);

        return ApiResponse.onSuccess(GeneralSuccessCode.CREATED,result);
    }

    @GetMapping("/{missionId}")
    @Operation(summary = "미션 상세 조회 API", description = "미션 정보와 사용자의 수행 상태를 조회합니다.")
    public ApiResponse<MissionResponseDto.MissionDetail> getMissionDetail(
            @PathVariable Long missionId,
            @AuthenticationPrincipal UserDetails userDetails
            ){
        Long memberId = Long.parseLong(userDetails.getUsername());

        MissionResponseDto.MissionDetail result = missionQueryService.getMissionDetail(memberId, missionId);

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
    }

    @PostMapping("/{missionId}/watch")
    @Operation(summary = "미션 영상 시청 완료 API", description = "영상을 끝까지 시청했을 때 호출합니다.")
    public ApiResponse<MissionResponseDto.WatchResult> updateWatchStatus(
            @PathVariable Long missionId,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        Long memberId = Long.parseLong(userDetails.getUsername());
        MissionResponseDto.WatchResult result = missionCommandService.updateMissionWatchStatus(memberId,missionId);

        return ApiResponse.onSuccess(GeneralSuccessCode.OK,result);
    }

    @PatchMapping("/{missionId}/status")
    @Operation(
            summary = "미션 상태 변경 (도전하기/찜/포기)",
            description = "미션의 상태를 변경합니다. 요청하는 `status` 값에 따라 동작이 다릅니다.\n\n" +
                    "1. **NONE (도전/찜 취소)**:\n" +
                    "   - 기본적으로 **시도 횟수가 +1 증가**합니다 (입장료 선불).\n" +
                    "   - 단, **현재 상태가 'SCRAP(찜)'인 경우**에는 **찜 취소**로 간주하여 **횟수가 증가하지 않습니다.**\n\n" +
                    "2. **SCRAP (찜하기)**: 찜 리스트로 이동합니다.\n" +
                    "3. **FAIL (포기)**: 재도전 리스트로 이동합니다."
            )
    public ApiResponse<MissionResponseDto.StatusResult> updateStatus(
            @PathVariable Long missionId,
            @RequestBody MissionRequestDto.PatchStatus request,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        Long memberId = Long.parseLong(userDetails.getUsername());
        MissionResponseDto.StatusResult result = missionCommandService.updateMissionStatus(memberId,missionId,request);

        return  ApiResponse.onSuccess(GeneralSuccessCode.OK,result);

    }

    @GetMapping("/keywords")
    @Operation(summary = "추천 키워드 목록 조회", description = "탐색에서 하단에 노출할 키워드들을 반환합니다.")
    public ApiResponse<MissionResponseDto.KeywordListResult> getRecommendKeyword(){
        MissionResponseDto.KeywordListResult result = missionQueryService.getRecommendedKeywords();
        return ApiResponse.onSuccess(GeneralSuccessCode.OK,result);
    }

    @GetMapping("/keywords/related")
    @Operation(summary = "연관 검색어(태그) 조회", description = "사용자가 입력한 검색어가 포함된 키워드(태그)를 최대 5개까지 추천해줍니다.")
    public ApiResponse<MissionResponseDto.KeywordListResult> getRelatedKeywords(
            @RequestParam String keyword
    ) {
        MissionResponseDto.KeywordListResult result = missionQueryService.getRelatedKeywords(keyword);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
    }

    @GetMapping("/recommend")
    @Operation(summary = "오늘의 추천 미션 리스트 조회(소요시간 + 세부관심사별)", description = "유저의 관심사와 자투리 시간을 반영하여 미션 5개를 추천합니다. 찜한 미션이 최상단에 노출됩니다.")
    public ApiResponse<List<MissionResponseDto.RecommendResult>> getTodayRecommendMissions(
       @RequestParam(required = false) Integer time,
       @AuthenticationPrincipal UserDetails userDetails
    ){
        Long memberId = Long.parseLong(userDetails.getUsername());
        List<MissionResponseDto.RecommendResult> result = missionQueryService.getTodayRecommendMissions(memberId,time);

        return ApiResponse.onSuccess(GeneralSuccessCode.OK,result);
    }

    @GetMapping("/search")
    @Operation(summary = "미션 검색 API", description = "검색어, 키워드로 미션을 검색합니다. 시드값 랜덤으로 검색할때 하나씩 보내주시면 됩니다. ")
    public ApiResponse<MissionResponseDto.SearchResponse> searchMissions(
       @AuthenticationPrincipal UserDetails userDetails,
       @RequestParam(required = false) String searchText,
       @RequestParam(required = false) List<String> keywords,
       @RequestParam(required = false) Long seed,
       @PageableDefault(size = 10) Pageable pageable
    ){
        Long memberId = Long.parseLong(userDetails.getUsername());

        return ApiResponse.onSuccess(GeneralSuccessCode.OK,
                missionQueryService.searchMissions(memberId, searchText, keywords, null, null, seed, pageable)
        );
    }

    @GetMapping("/categories")
    @Operation(summary = "카테고리별 미션 조회 API", description = "대분류(categoryId) 또는 소분류(subCategoryId)로 미션을 조회합니다.")
    public ApiResponse<MissionResponseDto.SearchResponse> getMissionsByCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long subCategoryId,
            @RequestParam(required = false) Long seed,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Long memberId = Long.parseLong(userDetails.getUsername());
        Long searchSeed = (seed != null) ? seed : 0L;

        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                missionQueryService.searchMissions(memberId, null, null, categoryId, subCategoryId, searchSeed, pageable)
        );
    }
}


