package com.example.moamoa_backend.domain.member.controller;

import com.example.moamoa_backend.domain.member.dto.req.MemberReqDto;
import com.example.moamoa_backend.domain.member.dto.res.MemberResDto;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 회원 관련 API 문서
 * - 회원 탈퇴, 프로필 조회/수정
 * - 팝업 설정
 */
@Tag(name = "Member API", description = "회원 관련 API")
public interface MemberControllerDocs {

	@Operation(
		summary = "회원 탈퇴",
		description = """
			회원의 계정을 삭제합니다.<br>
			
			**[인증 필요]**
			
			Header에 유효한 Access Token이 필요합니다.<br>
			Authorization: Bearer {accessToken}
			
			**[동작 방식]**
			
			Soft Delete 방식으로 처리되며, 이후 복구가 가능합니다.<br>
			서버에 저장된 Refresh Token이 삭제됩니다.<br>
			쿠키에 저장된 Refresh Token이 제거됩니다.<br>
			
			**[주의]**
			
			정지(BANNED) 상태의 계정은 탈퇴할 수 없습니다.<br>
			이미 탈퇴 요청된 계정은 중복 요청할 수 없습니다.<br>
			탈퇴 후 계정 복구는 탈퇴 계정 복구 API를 통해 가능합니다.<br>
			"""
	)
	ApiResponse<Void> deleteMember(
		@AuthenticationPrincipal UserDetails userDetails,
		HttpServletResponse response
	);

	@Operation(
		summary = "내 프로필 조회",
		description = """
			로그인한 회원의 프로필 정보를 조회합니다.<br>
			
			**[인증 필요]**
			
			Header에 유효한 Access Token이 필요합니다.<br>
			Authorization: Bearer {accessToken}
			
			**[응답 정보]**
			
			프로필 이미지, 이름, 생년월일, 성별 등 회원의 기본 프로필 정보가 반환됩니다.<br>
			"""
	)
	ApiResponse<MemberResDto.ProfileResponse> getProfile(
		@AuthenticationPrincipal UserDetails userDetails
	);

	@Operation(
		summary = "내 프로필 수정",
		description = """
			로그인한 회원의 프로필 정보를 수정합니다.<br>
			
			**[인증 필요]**
			
			Header에 유효한 Access Token이 필요합니다.<br>
			Authorization: Bearer {accessToken}
			
			**[수정 가능 항목]**
			
			프로필 이미지 (필수, 1~3 사이 값)<br>
			이름 (필수, 2~50자)<br>
			생년월일 (선택)<br>
			성별 (선택, MALE 또는 FEMALE)<br>
			
			**[주의]**
			
			프로필 이미지와 이름은 필수 값이므로 반드시 포함해야 합니다.<br>
			유효하지 않은 성별 값 입력 시 오류가 반환됩니다.<br>
			"""
	)
	ApiResponse<Void> updateProfile(
		@AuthenticationPrincipal UserDetails userDetails,
		@Valid @RequestBody MemberReqDto.ProfileUpdate request
	);

	@Operation(
		summary = "팝업 다시 보지 않기 설정",
		description = """
			특정 팝업에 대해 다시 보지 않기(NEVER_SHOW) 설정을 저장합니다.<br>
			
			**[인증 필요]**
			
			Header에 유효한 Access Token이 필요합니다.<br>
			Authorization: Bearer {accessToken}
			
			**[동작 방식]**
			
			해당 settingKey에 대한 설정이 이미 존재하면 값을 업데이트합니다.<br>
			설정이 존재하지 않으면 새로 생성합니다.<br>
			설정 후 해당 팝업은 더 이상 표시되지 않습니다.<br>
			"""
	)
	ApiResponse<Void> saveSetting(
		@AuthenticationPrincipal UserDetails userDetails,
		@Valid @RequestBody MemberReqDto.SettingRequest request
	);
}