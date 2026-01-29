package com.example.moamoa_backend.member.controller;

import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.member.dto.req.MemberReqDto;
import com.example.moamoa_backend.member.dto.res.MemberResDto;
import com.example.moamoa_backend.member.exception.code.MemberSuccessCode;
import com.example.moamoa_backend.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "비밀번호 변경", description = "로컬 로그인 회원의 비밀번호를 변경합니다.")
    @PatchMapping("/me/password")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MemberReqDto.PasswordChange request
    ) {
        memberService.changePassword(Long.parseLong(userDetails.getUsername()), request);
        return ApiResponse.onSuccess(MemberSuccessCode.PASSWORD_CHANGED, null);
    }

    @Operation(summary = "회원 탈퇴", description = "회원의 계정을 삭제합니다. (soft delete, 복구가능)")
    @DeleteMapping("/me")
    public ApiResponse<Void> deleteMember(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        memberService.deleteMember(Long.parseLong(userDetails.getUsername()));
        return ApiResponse.onSuccess(MemberSuccessCode.MEMBER_WITHDRAW, null);
    }

    @Operation(summary = "내 프로필 조회", description = "로그인한 회원의 프로필 정보를 조회합니다.")
    @GetMapping("/me/profile")
    public ApiResponse<MemberResDto.ProfileResponse> getProfile(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MemberResDto.ProfileResponse response = memberService.getProfile(Long.parseLong(userDetails.getUsername()));
        return ApiResponse.onSuccess(MemberSuccessCode.PROFILE_FETCHED, response);
    }

    @Operation(summary = "내 프로필 수정", description = "로그인한 회원의 프로필 정보를 수정합니다.")
    @PutMapping("/me/profile")
    public ApiResponse<Void> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MemberReqDto.ProfileUpdate request
    ) {
        memberService.updateProfile(Long.parseLong(userDetails.getUsername()), request);
        return ApiResponse.onSuccess(MemberSuccessCode.MEMBER_UPDATED, null);
    }
}