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
}


