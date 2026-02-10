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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;
    private final MemberSettingService memberSettingService;
    private final CookieUtil cookieUtil;

    @Operation(summary = "회원 탈퇴", description = "회원의 계정을 삭제합니다. (soft delete, 복구가능)")
    @DeleteMapping("/me")
    public ApiResponse<Void> deleteMember(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletResponse response
    ) {
        memberService.deleteMember(Long.parseLong(userDetails.getUsername()));
        cookieUtil.clearRefreshTokenCookie(response);
        return ApiResponse.onSuccess(MemberSuccessCode.MEMBER_WITHDRAW, null);
    }

    @Operation(summary = "내 프로필 조회", description = "로그인한 회원의 프로필 정보를 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<MemberResDto.ProfileResponse> getProfile(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MemberResDto.ProfileResponse response = memberService.getProfile(Long.parseLong(userDetails.getUsername()));
        return ApiResponse.onSuccess(MemberSuccessCode.PROFILE_FETCHED, response);
    }

    @Operation(summary = "내 프로필 수정", description = "로그인한 회원의 프로필 정보를 수정합니다.")
    @PutMapping("/me")
    public ApiResponse<Void> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MemberReqDto.ProfileUpdate request
    ) {
        memberService.updateProfile(Long.parseLong(userDetails.getUsername()), request);
        return ApiResponse.onSuccess(MemberSuccessCode.MEMBER_UPDATED, null);
    }

    @Operation(summary = "팝업 다시 보지 않기 설정", description = "특정 팝업에 대해 다시 보지 않기(NEVER_SHOW) 설정을 저장합니다")
    @PostMapping("/me/dismissed-popups")
    public ApiResponse<Void> saveSetting(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MemberReqDto.SettingRequest request
    ) {
        memberSettingService.dismissPopup(
                Long.parseLong(userDetails.getUsername()),
                request.settingKey()
        );
        return ApiResponse.onSuccess(MemberSuccessCode.SETTING_SAVED, null);
    }
}