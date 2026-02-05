package com.example.moamoa_backend.mission.controller;


import com.example.moamoa_backend.global.apiPayload.code.GeneralSuccessCode;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.mission.dto.request.MissionRequestDto;
import com.example.moamoa_backend.mission.dto.response.MissionResponseDto;
import com.example.moamoa_backend.mission.service.command.MissionCommandService;
import com.example.moamoa_backend.mission.service.query.MissionQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
            summary = "미션 등록 API(관리자용)",
            description = """
            관리자가 새로운 미션을 등록하는 API입니다.
            
            **[기능 상세]**
            - **영상 길이 자동 계산**: 입력된 유튜브 URL을 분석해 영상 시간(초)과 소요 시간(분)을 서버가 자동으로 저장합니다.
            - **보상 자동 계산**: 등록된 퀴즈의 타입별 배점에 따라 총 보상(도토리)이 자동으로 합산됩니다.
            
            **[키워드 타입 가이드]**
            - **SITUATION**: 추천상황 
            - **SKILL**: 획득스킬 
            - **KEYWORD**: 일반 키워드 
                                
            **[주의사항]**
            - **카테고리**: 반드시 '카테고리 코드표'의 소분류 한글명을 정확히 입력해야 매핑됩니다. (ex: "경제의 흐름")
            - **퀴즈 옵션**: OX는 `["O", "X"]`, 단답형은 `[]` 빈 배열을 준수해주세요.
            """
    )
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
    @Operation(
            summary = "미션 영상 시청 완료 (필수)",
            description = """
                    영상을 끝까지 시청했을 때 호출합니다.
                    
                    **[중요]**
                    - 이 API를 호출하여 **시청 완료(`isContentWatched=true`)** 상태가 되어야만,
                    - **`PATCH /missions/{id}/status` (도전하기)** API를 호출할 수 있습니다.
                    - (안 그러면 400 에러 뜸)
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "시청 완료 처리 성공 (이제 도전 가능)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "미션 정보를 찾을 수 없음")
    })
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
            description = """
                    미션의 상태를 변경합니다. 유저의 현재 상태와 요청 값에 따라 동작이 달라집니다.
                    
                    | 요청 상태 (`status`) | 동작 설명 | 비고 |
                    | :--- | :--- | :--- |
                    | **NONE** | **도전하기 / 재도전 / 찜 취소** | 기본적으로 시도 횟수가 1 증가합니다. <br> 단, `찜(SCRAP)` 상태에서 취소하는 경우엔 횟수가 늘지 않습니다. |
                    | **SCRAP** | **찜하기** | 미션을 찜 보관함으로 이동시킵니다. (성공한 미션은 불가) |
                    | **FAIL** | **포기하기** | 미션을 포기하고 재도전 리스트로 보냅니다. (시작 전에는 불가) |
                    
                    **[주의사항]**
                    - `SUCCESS`(성공) 상태로는 이 API로 변경할 수 없습니다. (정답 제출 API 사용)
                    """
            )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상태 변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "실패 이유:\n" +
                    "- `MISSION400_2`: 잘못된 파라미터\n" +
                    "- `MISSION400_3`: 시작 전 포기 시도\n" +
                    "- `MISSION400_4`: 유효하지 않은 상태값 파라미터\n" +
                    "- `MISSION400_7`: 영상 시청하지 않고 도전 시도"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "미션 정보를 찾을 수 없음")
    })
    public ApiResponse<MissionResponseDto.StatusResult> updateStatus(
            @PathVariable Long missionId,
            @Valid @RequestBody MissionRequestDto.PatchStatus request,
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

    @GetMapping("/me")
    @Operation(
            summary = "내 미션 보관함 조회 (찜/완료/다시풀기)",
            description = """
        내 미션 상태에 따라 목록을 조회합니다 (무한 스크롤 지원).
        
        **파라미터 설명:**
        - `status` (필수):
            - **SCRAP**: 찜한 미션
            - **COMPLETE**: 성공(SUCCESS)한 미션
            - **RETRY**: 실패(FAIL)했거나, 풀다가 중단한(NONE + 시도횟수>0) 미션
        - `condition` (선택, 기본값 LATEST):
            - **LATEST**: 최근 활동(저장/완료/시도) 순
            - **TIME_ASC**: 소요시간 짧은 순
            - **TIME_DESC**: 소요시간 긴 순
        - `categoryId` (선택): 대분류 ID로 필터링
        """
    )
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

    @PostMapping("/{missionId}/submit")
    @Operation(summary = "미션 정답 제출 및 채점",
            description = """
                   **[기능 설명]**
                   유저의 퀴즈 답안을 채점하고 성공 여부와 보상을 반환합니다.
                   
                   **[보상 지급 정책]**
                   이 API는 **'최초 시도'** 여부에 따라 결과가 다릅니다.
                   
                   1. 첫 시도 & 정답 
                      - 성공 처리 (`isSuccess: true`)
                      - **미션 보상 지급됨**
                      - **일간/주간 목표 카운트 인정됨** (목표 달성 시 추가 보상)
                      
                   2. 첫 시도 & 오답 
                      - 실패 처리 (`isSuccess: false`)
                      - 보상 없음
                      
                   3. 재시도 & 정답 (Retry) 
                      - 성공 처리 (`isSuccess: true`)
                      - **보상 없음 (`missionReward: 0`)**
                      - **목표 카운트 인정 안 됨** (이미 실패했거나 기록이 있으므로 제외)
                   
                   **[요청 형식]**
                   - `submissions`: 퀴즈 ID와 답안 리스트 (모든 문제 답안 필수)
                   """)
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


