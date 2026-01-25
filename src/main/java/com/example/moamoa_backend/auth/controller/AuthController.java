package com.example.moamoa_backend.auth.controller;

import com.example.moamoa_backend.auth.converter.AuthConverter;
import com.example.moamoa_backend.auth.dto.req.AuthReqDto;
import com.example.moamoa_backend.auth.dto.res.AuthResDto;
import com.example.moamoa_backend.auth.exception.code.AuthSuccessCode;
import com.example.moamoa_backend.auth.service.AuthService;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.global.util.NetworkUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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

    @Operation(summary = "이메일 인증번호 전송", description = "회원가입을 위해 이메일로 인증번호(6자리)를 전송합니다.")
    @SecurityRequirements(value = {})
    @PostMapping("/email/send-verification")
    public ApiResponse<Void> sendEmailAuthCode(
            @RequestBody @Valid AuthReqDto.EmailSendDto request,
            HttpServletRequest httpServletRequest
    ) {
        String clientIp = NetworkUtil.getClientIp(httpServletRequest);
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
    public ApiResponse<AuthResDto.TokenDto> login(
            @RequestBody @Valid AuthReqDto.LoginDto request,
            HttpServletResponse response
    ) {
        AuthResDto.GeneratedTokenDto generatedTokenDto = authService.login(request);
        setRefreshTokenCookie(response, generatedTokenDto.refreshToken());
        return ApiResponse.onSuccess(AuthSuccessCode.LOGIN_SUCCESS, AuthConverter.toTokenDto(generatedTokenDto));
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
        setRefreshTokenCookie(response, generatedTokenDto.refreshToken());
        return ApiResponse.onSuccess(AuthSuccessCode.REISSUE_SUCCESS, AuthConverter.toTokenDto(generatedTokenDto));
    }

    @Operation(summary = "로그아웃 API", description = "Redis에서 해당 사용자의 Refresh Token을 삭제합니다. (Header에 Access Token 필요)")
    @PostMapping("/logout")
    public ApiResponse<String> logout(@AuthenticationPrincipal UserDetails userDetails, HttpServletResponse response) {
        Long memberId = Long.parseLong(userDetails.getUsername());
        authService.logout(memberId);
        clearRefreshTokenCookie(response);
        return ApiResponse.onSuccess(AuthSuccessCode.LOGOUT_SUCCESS, null);
    }

    @Operation(summary = "소셜로그인 초기 토큰 발급 API", description = "소셜 로그인 이후 redirect URL의 code 파라미터를 이용해 accessToken을 발급받습니다.")
    @SecurityRequirements(value = {})
    @PostMapping("/oauth2/token")
    public ApiResponse<AuthResDto.TokenDto> getAccessToken(@RequestBody @Valid AuthReqDto.OAuthLoginReqDto request) {
        return ApiResponse.onSuccess(AuthSuccessCode.LOGIN_SUCCESS, authService.exchangeAuthCode(request.code()));
    }

    @Operation(summary = "계정복구 요청", description = "삭제 요청된 회원에 대한 복구를 진행합니다.")
    @SecurityRequirements(value = {})
    @PostMapping("/recover")
    public ApiResponse<AuthResDto.TokenDto> recover(
            @RequestBody @Valid AuthReqDto.LoginDto request,
            HttpServletResponse response
    ) {
        AuthResDto.GeneratedTokenDto generatedTokenDto = authService.recover(request);
        setRefreshTokenCookie(response, generatedTokenDto.refreshToken());
        return ApiResponse.onSuccess(AuthSuccessCode.RECOVER_SUCCESS, AuthConverter.toTokenDto(generatedTokenDto));
    }

    // ----- Helper Methods -----

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .path("/")
                .sameSite("None")
                .httpOnly(true)
                .secure(true)
                .maxAge(14 * 24 * 60 * 60)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .path("/")
                .sameSite("None")
                .httpOnly(true)
                .secure(true)
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}