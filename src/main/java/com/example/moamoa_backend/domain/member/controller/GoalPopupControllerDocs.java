package com.example.moamoa_backend.domain.member.controller;

import com.example.moamoa_backend.domain.member.dto.res.GoalPopupResponseDto;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "GoalPopup API", description = "목표 실패 팝업 조회 및 팝업 확인 처리 API")
public interface GoalPopupControllerDocs {

	@Operation(
		summary = "목표 실패 팝업 조회",
		description = """
			홈 진입 시 띄워야 하는 **목표 실패 팝업** 목록을 조회합니다.<br><br>
			
			**[정책]**<br>
			• 성공 팝업은 '답안 제출 즉시' 처리(별도 UX)하며, 홈에서는 노출하지 않습니다.<br>
			• 홈에서는 오직 "전일/전주 실패"만 조회합니다.<br><br>
			
			**[조회 범위]**<br>
			• DAILY: 어제(goalDate = yesterday)의 실패 결과 중, 아직 안 본 것(popupShownAt == null)<br>
			• WEEKLY: 지난 주 일요일 종료(goalDate = lastWeekEnd)의 실패 결과 중, 아직 안 본 것(popupShownAt == null)<br><br>
			
			**[반환 규칙]**<br>
			• 노출할 팝업이 없으면 빈 배열([])을 반환합니다.<br>
			• 조회만으로는 소진되지 않으며, 프론트가 실제로 띄운 뒤 shown API를 호출해야 소진(봤음 처리)됩니다.
			"""
	)
	ApiResponse<GoalPopupResponseDto> getGoalPopups(
		@AuthenticationPrincipal UserDetails userDetails
	);

	@Operation(
		summary = "목표 팝업 확인 처리",
		description = """
			특정 goalResult 팝업을 **'봤음'** 으로 처리합니다.<br><br>
			
			**[요청 조건]**<br>
			• 프론트에서 팝업을 실제로 띄운 뒤 호출해야 합니다.<br><br>
			
			**[권한/무결성]**<br>
			• goalResultId는 반드시 '내 것'이어야 합니다(소유자 검증).<br><br>
			
			**[멱등성]**<br>
			• 이미 본 팝업(popupShownAt != null)을 다시 호출해도 상태는 유지됩니다.
			"""
	)
	ApiResponse<Void> markGoalPopupShown(
		@AuthenticationPrincipal UserDetails userDetails,
		@PathVariable Long goalResultId
	);
}
