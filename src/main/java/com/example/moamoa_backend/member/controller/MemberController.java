package com.example.moamoa_backend.member.controller;

import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.member.dto.req.MemberReqDto;
import com.example.moamoa_backend.member.dto.res.MemberResDto;
import com.example.moamoa_backend.member.exception.code.MemberSuccessCode;
import com.example.moamoa_backend.member.service.MemberService;
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

    @PatchMapping("/me/password")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MemberReqDto.PasswordChange request
    ) {
        memberService.changePassword(Long.parseLong(userDetails.getUsername()), request);
        return ApiResponse.onSuccess(MemberSuccessCode.PASSWORD_CHANGED, null);
    }

    @DeleteMapping("/me")
    public ApiResponse<Void> deleteMember(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        memberService.deleteMember(Long.parseLong(userDetails.getUsername()));
        return ApiResponse.onSuccess(MemberSuccessCode.MEMBER_WITHDRAW, null);
    }

    @GetMapping("/me/profile")
    public ApiResponse<MemberResDto.ProfileResponse> getProfile(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MemberResDto.ProfileResponse response = memberService.getProfile(Long.parseLong(userDetails.getUsername()));
        return ApiResponse.onSuccess(MemberSuccessCode.PROFILE_FETCHED, response);
    }
}