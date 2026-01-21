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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
}


