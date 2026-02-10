package com.example.moamoa_backend.domain.member.controller;

import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.global.util.CookieUtil;
import com.example.moamoa_backend.domain.member.dto.req.MemberReqDto;
import com.example.moamoa_backend.domain.member.dto.res.MemberResDto;
import com.example.moamoa_backend.domain.member.exception.code.MemberSuccessCode;
import com.example.moamoa_backend.domain.member.service.MemberService;
import com.example.moamoa_backend.domain.member.service.MemberSettingService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController implements MemberControllerDocs {

    private final MemberService memberService;
    private final MemberSettingService memberSettingService;
    private final CookieUtil cookieUtil;

    @Override
    @DeleteMapping("/me")
    public ApiResponse<Void> deleteMember(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletResponse response
    ) {
        memberService.deleteMember(Long.parseLong(userDetails.getUsername()));
        cookieUtil.clearRefreshTokenCookie(response);
        return ApiResponse.onSuccess(MemberSuccessCode.MEMBER_WITHDRAW, null);
    }

    @Override
    @GetMapping("/me")
    public ApiResponse<MemberResDto.ProfileResponse> getProfile(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MemberResDto.ProfileResponse response = memberService.getProfile(Long.parseLong(userDetails.getUsername()));
        return ApiResponse.onSuccess(MemberSuccessCode.PROFILE_FETCHED, response);
    }

    @Override
    @PutMapping("/me")
    public ApiResponse<Void> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MemberReqDto.ProfileUpdate request
    ) {
        memberService.updateProfile(Long.parseLong(userDetails.getUsername()), request);
        return ApiResponse.onSuccess(MemberSuccessCode.MEMBER_UPDATED, null);
    }

    @Override
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