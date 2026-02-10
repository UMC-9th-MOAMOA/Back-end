package com.example.moamoa_backend.domain.member.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.domain.member.dto.res.GoalPopupResponseDto;
import com.example.moamoa_backend.domain.member.exception.code.MemberSuccessCode;
import com.example.moamoa_backend.domain.member.service.GoalResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "GoalPopup", description = "목표 실패 팝업 조회 및 팝업 확인 처리 API")
public interface GoalPopupControllerDocs {
	@Operation(
		summary = "목표 실패 팝업 조회",
		description = """
            홈 진입 시 띄워야 하는 목표 팝업 목록을 반환합니다.
            - DAILY: 어제 결과(실패) 중 아직 안 본 것
            - WEEKLY: 지난 주 결과(일요일 종료) 중 아직 안 본 것
            """
	)
	public ApiResponse<GoalPopupResponseDto> getGoalPopups(
		@AuthenticationPrincipal UserDetails userDetails
	) ;

	@Operation(
		summary = "목표 팝업 확인 처리",
		description = "goalResultId에 해당하는 목표 팝업을 '봤음'으로 처리합니다."
	)
	public ApiResponse<Void> markGoalPopupShown(
		@AuthenticationPrincipal UserDetails userDetails,
		@PathVariable Long goalResultId
	) ;

}
