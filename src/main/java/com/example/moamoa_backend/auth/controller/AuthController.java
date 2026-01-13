package com.example.moamoa_backend.auth.controller;

import com.example.moamoa_backend.auth.dto.req.AuthReqDto;
import com.example.moamoa_backend.auth.exception.code.AuthSuccessCode;
import com.example.moamoa_backend.auth.service.AuthService;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    @PostMapping("/email/send-verification")
    public ApiResponse<Void> sendEmailAuthCode(@RequestBody @Valid AuthReqDto.EmailSendDto request) {
        authService.sendEmailAuthCode(request.email());
        return ApiResponse.onSuccess(AuthSuccessCode.EMAIL_SEND_SUCCESS, null);
    }

    @Operation(summary = "이메일 인증번호 검증", description = "사용자가 입력한 인증번호를 검증합니다.")
    @PostMapping("/email/verify")
    public ApiResponse<Void> verifyEmailAuthCode(@RequestBody @Valid AuthReqDto.EmailVerifyDto request) {
        authService.verifyEmailAuthCode(request.email(), request.authCode());
        return ApiResponse.onSuccess(AuthSuccessCode.EMAIL_VERIFY_SUCCESS, null);
    }

    @Operation(summary = "회원가입", description = "이메일 인증이 완료된 요청에 대해 회원가입을 진행합니다.")
    @PostMapping("/signup")
    public ApiResponse<Void> signup(@RequestBody @Valid AuthReqDto.SignupDto request) {
        authService.signup(request);
        return ApiResponse.onSuccess(AuthSuccessCode.SIGNUP_SUCCESS, null);
    }
}