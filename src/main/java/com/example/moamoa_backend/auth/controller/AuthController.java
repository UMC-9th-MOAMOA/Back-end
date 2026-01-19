package com.example.moamoa_backend.auth.controller;

import com.example.moamoa_backend.auth.dto.req.AuthReqDto;
import com.example.moamoa_backend.auth.dto.req.AuthResDto;
import com.example.moamoa_backend.auth.exception.code.AuthSuccessCode;
import com.example.moamoa_backend.auth.service.AuthService;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.global.util.NetworkUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "이메일 인증번호 전송", description = "회원가입을 위해 이메일로 인증번호(6자리)를 전송합니다.")
    @SecurityRequirements(value = {})
    @PostMapping("/email/send-verification")
    public ApiResponse<Void> sendEmailAuthCode(
            @RequestBody @Valid AuthReqDto.EmailSendDto request,
            HttpServletRequest httpServletRequest
            ) {
        String clientIp = NetworkUtil.getClientIp(httpServletRequest); //IP 추출
        authService.sendEmailAuthCode(request.email(), clientIp);
        return ApiResponse.onSuccess(AuthSuccessCode.EMAIL_SEND_SUCCESS, null);
    }

    @Operation(summary = "이메일 인증번호 검증", description = "사용자가 입력한 인증번호를 검증합니다.")
    @SecurityRequirements(value = {})
    @PostMapping("/email/verify")
    public ApiResponse<Void> verifyEmailAuthCode(@RequestBody @Valid AuthReqDto.EmailVerifyDto request) {
        authService.verifyEmailAuthCode(request.email(), request.authCode());
        return ApiResponse.onSuccess(AuthSuccessCode.EMAIL_VERIFY_SUCCESS, null);
    }

    @Operation(summary = "회원가입", description = "이메일 인증이 완료된 요청에 대해 회원가입을 진행합니다.")
    @SecurityRequirements(value = {})
    @PostMapping("/signup")
    public ApiResponse<Void> signup(@RequestBody @Valid AuthReqDto.SignupDto request) {
        authService.signup(request);
        return ApiResponse.onSuccess(AuthSuccessCode.SIGNUP_SUCCESS, null);
    }

    @Operation(summary = "일반 로그인 API", description = "이메일과 비밀번호로 로그인하여 Access/Refresh Token을 발급받습니다.")
    @SecurityRequirements(value = {})
    @PostMapping("/login")
    public ApiResponse<AuthResDto.TokenDto> login(@RequestBody @Valid AuthReqDto.LoginDto request) {
        AuthResDto.TokenDto result = authService.login(request);
        return ApiResponse.onSuccess(AuthSuccessCode.LOGIN_SUCCESS, result);
    }

    @Operation(summary = "토큰 재발급 API", description = "Refresh Token을 이용하여 Access Token과 Refresh Token을 재발급(RTR)받습니다.")
    @SecurityRequirements(value = {})
    @PostMapping("/reissue")
    public ApiResponse<AuthResDto.TokenDto> reissue(@RequestBody @Valid AuthReqDto.ReissueDto request) {
        AuthResDto.TokenDto result = authService.reissue(request);
        return ApiResponse.onSuccess(AuthSuccessCode.REISSUE_SUCCESS, result);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃 API", description = "Redis에서 해당 사용자의 Refresh Token을 삭제합니다. (Header에 Access Token 필요)")
    public ApiResponse<String> logout(@AuthenticationPrincipal UserDetails userDetails) {
        // SecurityContext에서 memberId 추출
        Long memberId = Long.parseLong(userDetails.getUsername());

        authService.logout(memberId);
        return ApiResponse.onSuccess(AuthSuccessCode.LOGOUT_SUCCESS, null);
    }
}