package com.example.moamoa_backend.domain.member.controller;

import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.global.util.CookieUtil;
import com.example.moamoa_backend.domain.member.dto.req.MemberReqDto;
import com.example.moamoa_backend.domain.member.dto.res.MemberResDto;
import com.example.moamoa_backend.domain.member.exception.code.MemberSuccessCode;
import com.example.moamoa_backend.domain.member.service.MemberService;
import com.example.moamoa_backend.domain.member.service.MemberSettingService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

public interface MemberControllerDocs {
    @Operation(summary = "회원 탈퇴", description = "회원의 계정을 삭제합니다. (soft delete, 복구가능)")
    public ApiResponse<Void> deleteMember(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletResponse response
    ) ;

    @Operation(summary = "내 프로필 조회", description = "로그인한 회원의 프로필 정보를 조회합니다.")
    public ApiResponse<MemberResDto.ProfileResponse> getProfile(
            @AuthenticationPrincipal UserDetails userDetails
    ) ;

    @Operation(summary = "내 프로필 수정", description = "로그인한 회원의 프로필 정보를 수정합니다.")
    public ApiResponse<Void> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MemberReqDto.ProfileUpdate request
    ) ;

    @Operation(summary = "팝업 다시 보지 않기 설정", description = "특정 팝업에 대해 다시 보지 않기(NEVER_SHOW) 설정을 저장합니다")
    public ApiResponse<Void> saveSetting(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MemberReqDto.SettingRequest request
    ) ;

}
