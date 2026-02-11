package com.example.moamoa_backend.domain.item.controller;

import com.example.moamoa_backend.domain.item.dto.HomePocketResponseDto;
import com.example.moamoa_backend.domain.item.dto.HomeResponseDto;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@Tag(name = "Home API", description = "홈 화면 관련 API")
public interface HomeControllerDocs {
    @Operation(
            summary = "홈 메인 조회",
            description = """
                    홈 메인 정보를 조회합니다.<br><br>

                    **[인증 필요]**<br>
                    Authorization: Bearer {accessToken}<br><br>

                    **[응답]**<br>
                    사용자 이름 / 내 도토리 개수 / 다람쥐 착장(배경 포함) 정보 반환
                    """
    )
    ApiResponse<HomeResponseDto> getHome(
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "홈 화면 주머니 조회",
            description = """
                    홈 화면 주머니 정보를 조회합니다.<br><br>

                    **[인증 필요]**<br>
                    Authorization: Bearer {accessToken}<br><br>

                    **[응답]**<br>
                    - todayMissionMinutes, thisWeekMissionMinutes<br>
                    - walletPoint<br>
                    - currentStreak<br>
                    - goalProgress(없으면 null)
                    """
    )
    ApiResponse<HomePocketResponseDto.Response> getHomePocket(
            @AuthenticationPrincipal UserDetails userDetails
    );
}
