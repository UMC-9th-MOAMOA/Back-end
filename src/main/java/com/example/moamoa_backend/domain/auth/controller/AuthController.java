package com.example.moamoa_backend.domain.auth.controller;

import com.example.moamoa_backend.domain.auth.converter.AuthConverter;
import com.example.moamoa_backend.domain.auth.dto.req.AuthReqDto;
import com.example.moamoa_backend.domain.auth.dto.res.AuthResDto;
import com.example.moamoa_backend.domain.auth.exception.code.AuthSuccessCode;
import com.example.moamoa_backend.domain.auth.service.AuthService;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.global.util.CookieUtil;
import com.example.moamoa_backend.global.util.NetworkUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API Controller
 * - 회원가입, 로그인, 토큰 재발급, 로그아웃
 * - 소셜 로그인 토큰 교환
 * - 탈퇴 계정 복구
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @Operation(summary = "이메일 인증번호 전송", description = "회원가입을 위해 이메일로 인증번호(6자리)를 전송합니다.")
    @SecurityRequirements(value = {})
    @PostMapping("/email/verification-codes")
    public ApiResponse<Void> sendEmailAuthCode(
            @RequestBody @Valid AuthReqDto.EmailSendDto request,
            HttpServletRequest httpServletRequest
    ) {
        String clientIp = NetworkUtil.getClientIp(httpServletRequest);
        authService.sendEmailAuthCode(request.email(), clientIp);
        return ApiResponse.onSuccess(AuthSuccessCode.EMAIL_SEND_SUCCESS, null);
    }

    @Operation(summary = "이메일 인증번호 검증", description = "사용자가 입력한 이메일 인증번호를 검증합니다.")
    @SecurityRequirements(value = {})
    @PostMapping("/email/verifications")
    public ApiResponse<Void> verifyEmailAuthCode(@RequestBody @Valid AuthReqDto.EmailVerifyDto request) {
        authService.verifyEmailAuthCode(request.email(), request.authCode());
        return ApiResponse.onSuccess(AuthSuccessCode.EMAIL_VERIFY_SUCCESS, null);
    }

    @Operation(summary = "회원가입", description = "이메일 인증이 완료된 요청에 대해 회원가입을 진행하고 자동 로그인을 진행합니다.")
    @SecurityRequirements(value = {})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/signup")
    public ApiResponse<AuthResDto.TokenDto> signup(
            @RequestBody @Valid AuthReqDto.SignupDto request,
            HttpServletResponse response
    ) {
        AuthResDto.GeneratedTokenDto generatedTokenDto = authService.signup(request);
        cookieUtil.setRefreshTokenCookie(response, generatedTokenDto.refreshToken());
        return ApiResponse.onSuccess(AuthSuccessCode.SIGNUP_SUCCESS, AuthConverter.toTokenDto(generatedTokenDto));
    }

    @Operation(summary = "일반 로그인 API", description = "이메일과 비밀번호로 로그인하여 Access/Refresh Token을 발급받습니다.")
    @SecurityRequirements(value = {})
    @PostMapping("/login")
    public ApiResponse<AuthResDto.LoginResponseDto> login(
            @RequestBody @Valid AuthReqDto.LoginDto request,
            HttpServletResponse response
    ) {
        AuthResDto.LoginResultDto result = authService.login(request);
        cookieUtil.setRefreshTokenCookie(response, result.generatedToken().refreshToken());
        return ApiResponse.onSuccess(AuthSuccessCode.LOGIN_SUCCESS, AuthConverter.toLoginResponseDto(result));
    }

    @Operation(summary = "토큰 재발급 API", description = "Refresh Token을 이용하여 Access Token과 Refresh Token을 재발급(RTR)받습니다.")
    @SecurityRequirements(value = {})
    @PostMapping("/refresh")
    public ApiResponse<AuthResDto.TokenDto> refresh(
            @Parameter(hidden = true)
            @CookieValue(name = "refreshToken", required = true) String refreshToken,
            HttpServletResponse response
    ) {
        AuthReqDto.ReissueDto request = new AuthReqDto.ReissueDto(refreshToken);
        AuthResDto.GeneratedTokenDto generatedTokenDto = authService.refresh(request);
        cookieUtil.setRefreshTokenCookie(response, generatedTokenDto.refreshToken());
        return ApiResponse.onSuccess(AuthSuccessCode.REISSUE_SUCCESS, AuthConverter.toTokenDto(generatedTokenDto));
    }

    @Operation(summary = "로그아웃 API", description = "Redis에서 해당 사용자의 Refresh Token을 삭제합니다. (Header에 Access Token 필요)")
    @PostMapping("/logout")
    public ApiResponse<String> logout(@AuthenticationPrincipal UserDetails userDetails, HttpServletResponse response) {
        Long memberId = Long.parseLong(userDetails.getUsername());
        authService.logout(memberId);
        cookieUtil.clearRefreshTokenCookie(response);
        return ApiResponse.onSuccess(AuthSuccessCode.LOGOUT_SUCCESS, null);
    }

    @Operation(summary = "소셜로그인 초기 토큰 발급 API", description = "소셜 로그인 이후 redirect URL의 code 파라미터를 이용해 accessToken을 발급받습니다.")
    @SecurityRequirements(value = {})
    @PostMapping("/oauth2/token")
    public ApiResponse<AuthResDto.LoginResponseDto> exchangeOAuthToken(
            @RequestBody @Valid AuthReqDto.OAuthLoginReqDto request,
            HttpServletResponse response
    ) {
        AuthResDto.LoginResultDto loginResultDto = authService.exchangeOAuthCode(request.code());
        cookieUtil.setRefreshTokenCookie(response, loginResultDto.generatedToken().refreshToken());
        return ApiResponse.onSuccess(AuthSuccessCode.LOGIN_SUCCESS, AuthConverter.toLoginResponseDto(loginResultDto));
    }

    @Operation(summary = "계정복구 요청", description = "삭제 요청된 회원에 대한 복구를 진행합니다.")
    @SecurityRequirements(value = {})
    @PostMapping("/recover")
    public ApiResponse<AuthResDto.TokenDto> recover(
            @RequestBody @Valid AuthReqDto.LoginDto request,
            HttpServletResponse response
    ) {
        AuthResDto.GeneratedTokenDto generatedTokenDto = authService.recover(request);
        cookieUtil.setRefreshTokenCookie(response, generatedTokenDto.refreshToken());
        return ApiResponse.onSuccess(AuthSuccessCode.RECOVER_SUCCESS, AuthConverter.toTokenDto(generatedTokenDto));
    }

    @Operation(summary = "비밀번호 초기화 코드 발송", description = "비밀번호 초기화를 위한 인증번호(6자리)를 이메일로 발송합니다.")
    @SecurityRequirements(value = {})
    @PostMapping("/password-resets")
    public ApiResponse<Void> sendPasswordResetCode(
            @RequestBody @Valid AuthReqDto.EmailSendDto request,
            HttpServletRequest httpServletRequest
    ) {
        String clientIp = NetworkUtil.getClientIp(httpServletRequest);
        authService.sendPasswordResetCode(request.email(), clientIp);
        return ApiResponse.onSuccess(AuthSuccessCode.EMAIL_SEND_SUCCESS, null);
    }

    @Operation(summary = "비밀번호 초기화 코드 검증", description = "인증번호를 검증하고 비밀번호 초기화에 사용할 토큰을 발급합니다.")
    @SecurityRequirements(value = {})
    @PostMapping("/password-resets/verifications")
    public ApiResponse<AuthResDto.PasswordResetTokenDto> verifyPasswordResetCode(
            @RequestBody @Valid AuthReqDto.EmailVerifyDto request
    ) {
        String resetToken = authService.verifyPasswordResetCode(request.email(), request.authCode());
        return ApiResponse.onSuccess(AuthSuccessCode.EMAIL_VERIFY_SUCCESS, new AuthResDto.PasswordResetTokenDto(resetToken));
    }

    @Operation(summary = "비밀번호 초기화", description = "토큰을 검증하고 새 비밀번호로 초기화합니다.")
    @SecurityRequirements(value = {})
    @PutMapping("/password-resets")
    public ApiResponse<Void> resetPassword(
            @RequestBody @Valid AuthReqDto.PasswordResetDto request
    ) {
        authService.resetPassword(request);
        return ApiResponse.onSuccess(AuthSuccessCode.PASSWORD_RESET_SUCCESS, null);
    }

    @Operation(summary = "비밀번호 변경", description = "로컬 로그인 회원의 비밀번호를 변경합니다.")
    @PatchMapping("/password")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AuthReqDto.PasswordChange request
    ) {
        authService.changePassword(Long.parseLong(userDetails.getUsername()), request);
        return ApiResponse.onSuccess(AuthSuccessCode.PASSWORD_CHANGED, null);
    }

}