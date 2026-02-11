package com.example.moamoa_backend.domain.auth.controller;

import com.example.moamoa_backend.domain.auth.converter.AuthConverter;
import com.example.moamoa_backend.domain.auth.dto.req.AuthReqDto;
import com.example.moamoa_backend.domain.auth.dto.res.AuthResDto;
import com.example.moamoa_backend.domain.auth.exception.code.AuthSuccessCode;
import com.example.moamoa_backend.domain.auth.service.AuthService;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.global.util.CookieUtil;
import com.example.moamoa_backend.global.util.NetworkUtil;

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
public class AuthController implements AuthControllerDocs {

	private final AuthService authService;
	private final CookieUtil cookieUtil;

	@Override
	@PostMapping("/email/verification-codes")
	public ApiResponse<Void> sendEmailAuthCode(
		@RequestBody @Valid AuthReqDto.EmailSendDto request,
		HttpServletRequest httpServletRequest
	) {
		String clientIp = NetworkUtil.getClientIp(httpServletRequest);
		authService.sendEmailAuthCode(request.email(), clientIp);
		return ApiResponse.onSuccess(AuthSuccessCode.EMAIL_SEND_SUCCESS, null);
	}

	@Override
	@PostMapping("/email/verifications")
	public ApiResponse<Void> verifyEmailAuthCode(@RequestBody @Valid AuthReqDto.EmailVerifyDto request) {
		authService.verifyEmailAuthCode(request.email(), request.authCode());
		return ApiResponse.onSuccess(AuthSuccessCode.EMAIL_VERIFY_SUCCESS, null);
	}

	@Override
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/signup")
	public ApiResponse<AuthResDto.LoginResponseDto> signup(
		@RequestBody @Valid AuthReqDto.SignupDto request,
		HttpServletResponse response
	) {
		AuthResDto.LoginResultDto result = authService.signup(request);
		cookieUtil.setRefreshTokenCookie(response, result.generatedToken().refreshToken());
		return ApiResponse.onSuccess(AuthSuccessCode.SIGNUP_SUCCESS, AuthConverter.toLoginResponseDto(result));
	}

	@Override
	@PostMapping("/login")
	public ApiResponse<AuthResDto.LoginResponseDto> login(
		@RequestBody @Valid AuthReqDto.LoginDto request,
		HttpServletResponse response
	) {
		AuthResDto.LoginResultDto result = authService.login(request);
		cookieUtil.setRefreshTokenCookie(response, result.generatedToken().refreshToken());
		return ApiResponse.onSuccess(AuthSuccessCode.LOGIN_SUCCESS, AuthConverter.toLoginResponseDto(result));
	}

	@Override
	@PostMapping("/refresh")
	public ApiResponse<AuthResDto.TokenDto> refresh(
		@CookieValue(name = "refreshToken", required = true) String refreshToken,
		HttpServletResponse response
	) {
		AuthReqDto.ReissueDto request = new AuthReqDto.ReissueDto(refreshToken);
		AuthResDto.GeneratedTokenDto generatedTokenDto = authService.refresh(request);
		cookieUtil.setRefreshTokenCookie(response, generatedTokenDto.refreshToken());
		return ApiResponse.onSuccess(AuthSuccessCode.REISSUE_SUCCESS, AuthConverter.toTokenDto(generatedTokenDto));
	}

	@Override
	@PostMapping("/logout")
	public ApiResponse<String> logout(@AuthenticationPrincipal UserDetails userDetails, HttpServletResponse response) {
		Long memberId = Long.parseLong(userDetails.getUsername());
		authService.logout(memberId);
		cookieUtil.clearRefreshTokenCookie(response);
		return ApiResponse.onSuccess(AuthSuccessCode.LOGOUT_SUCCESS, null);
	}

	@Override
	@PostMapping("/oauth2/token")
	public ApiResponse<AuthResDto.LoginResponseDto> exchangeOAuthToken(
		@RequestBody @Valid AuthReqDto.OAuthLoginReqDto request,
		HttpServletResponse response
	) {
		AuthResDto.LoginResultDto loginResultDto = authService.exchangeOAuthCode(request.code());
		cookieUtil.setRefreshTokenCookie(response, loginResultDto.generatedToken().refreshToken());
		return ApiResponse.onSuccess(AuthSuccessCode.LOGIN_SUCCESS, AuthConverter.toLoginResponseDto(loginResultDto));
	}

	@Override
	@PostMapping("/recover")
	public ApiResponse<AuthResDto.TokenDto> recover(
		@RequestBody @Valid AuthReqDto.LoginDto request,
		HttpServletResponse response
	) {
		AuthResDto.GeneratedTokenDto generatedTokenDto = authService.recover(request);
		cookieUtil.setRefreshTokenCookie(response, generatedTokenDto.refreshToken());
		return ApiResponse.onSuccess(AuthSuccessCode.RECOVER_SUCCESS, AuthConverter.toTokenDto(generatedTokenDto));
	}

	@Override
	@PostMapping("/password-resets")
	public ApiResponse<Void> sendPasswordResetCode(
		@RequestBody @Valid AuthReqDto.EmailSendDto request,
		HttpServletRequest httpServletRequest
	) {
		String clientIp = NetworkUtil.getClientIp(httpServletRequest);
		authService.sendPasswordResetCode(request.email(), clientIp);
		return ApiResponse.onSuccess(AuthSuccessCode.EMAIL_SEND_SUCCESS, null);
	}

	@Override
	@PostMapping("/password-resets/verifications")
	public ApiResponse<AuthResDto.PasswordResetTokenDto> verifyPasswordResetCode(
		@RequestBody @Valid AuthReqDto.EmailVerifyDto request
	) {
		String resetToken = authService.verifyPasswordResetCode(request.email(), request.authCode());
		return ApiResponse.onSuccess(AuthSuccessCode.EMAIL_VERIFY_SUCCESS,
			new AuthResDto.PasswordResetTokenDto(resetToken));
	}

	@Override
	@PutMapping("/password-resets")
	public ApiResponse<Void> resetPassword(
		@RequestBody @Valid AuthReqDto.PasswordResetDto request
	) {
		authService.resetPassword(request);
		return ApiResponse.onSuccess(AuthSuccessCode.PASSWORD_RESET_SUCCESS, null);
	}

	@Override
	@PatchMapping("/password")
	public ApiResponse<Void> changePassword(
		@AuthenticationPrincipal UserDetails userDetails,
		@Valid @RequestBody AuthReqDto.PasswordChange request
	) {
		authService.changePassword(Long.parseLong(userDetails.getUsername()), request);
		return ApiResponse.onSuccess(AuthSuccessCode.PASSWORD_CHANGED, null);
	}

}