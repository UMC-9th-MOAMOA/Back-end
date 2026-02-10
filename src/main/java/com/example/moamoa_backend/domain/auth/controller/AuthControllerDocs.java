package com.example.moamoa_backend.domain.auth.controller;

import com.example.moamoa_backend.domain.auth.dto.req.AuthReqDto;
import com.example.moamoa_backend.domain.auth.dto.res.AuthResDto;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API 문서
 * - 회원가입, 로그인, 토큰 재발급, 로그아웃
 * - 소셜 로그인 토큰 교환
 * - 탈퇴 계정 복구
 */
@Tag(name = "Auth API", description = "인증 관련 API")
public interface AuthControllerDocs {

    @Operation(
            summary = "이메일 인증번호 발송",
            description = """
                    회원가입을 위해 입력한 이메일 주소로 6자리 인증번호를 발송합니다.<br>

                    **[요청 조건]**

                    유효한 이메일 형식이어야 합니다.<br>
                    이미 가입된 이메일로는 발송할 수 없습니다.<br>

                    **[유효기간]**

                    발송 후 3분간 유효합니다.<br>
                    유효기간 만료 시 재발송이 필요합니다.<br>

                    **[동작 방식]**

                    입력한 이메일 주소로 인증번호가 전송됩니다.<br>
                    이후 이메일 인증번호 검증 API를 호출하여 인증을 완료해야 합니다.<br>
                    """
    )
    @SecurityRequirements(value = {})
    ApiResponse<Void> sendEmailAuthCode(
            @RequestBody @Valid AuthReqDto.EmailSendDto request,
            HttpServletRequest httpServletRequest
    );

    @Operation(
            summary = "이메일 인증번호 검증",
            description = """
                    발송된 6자리 인증번호를 검증합니다.<br>

                    **[요청 조건]**

                    인증번호 발송 API를 먼저 호출해야 합니다.<br>
                    발송된 인증번호와 정확히 일치해야 합니다.<br>

                    **[성공]**

                    해당 이메일은 5분간 인증 완료 상태로 저장됩니다.<br>
                    이 시간 내에 회원가입 API를 호출하여 가입을 완료해야 합니다.<br>

                    **[실패]**

                    인증번호가 일치하지 않거나 만료된 경우 실패합니다.<br>
                    인증번호를 재발송하여 다시 시도할 수 있습니다.<br>
                    """
    )
    @SecurityRequirements(value = {})
    ApiResponse<Void> verifyEmailAuthCode(@RequestBody @Valid AuthReqDto.EmailVerifyDto request);

    @Operation(
            summary = "회원가입",
            description = """
                    이메일 인증이 완료된 사용자의 회원가입을 처리합니다.<br>

                    **[요청 조건]**

                    이메일 인증번호 검증이 완료된 상태여야 합니다.<br>
                    인증번호 검증 후 5분 이내에 요청해야 합니다.<br>
                    비밀번호는 보안 정책에 맞게 설정해야 합니다.<br>

                    **[성공]**

                    자동으로 로그인 처리됩니다.<br>
                    Access Token → 응답 Body에 포함
                    Refresh Token → HttpOnly 쿠키로 전달

                    **[토큰 정보]**

                    | 토큰 | 전달 방식 | 용도 |
                    |------|-----------|------|
                    | Access Token | Response Body | API 인증 헤더에 사용 |
                    | Refresh Token | HttpOnly Cookie | 토큰 재발급 시 사용 |
                    """
    )
    @SecurityRequirements(value = {})
    ApiResponse<AuthResDto.TokenDto> signup(
            @RequestBody @Valid AuthReqDto.SignupDto request,
            HttpServletResponse response
    );

    @Operation(
            summary = "일반 로그인",
            description = """
                    이메일과 비밀번호를 사용하여 일반 로그인합니다.<br>

                    **[요청 조건]**

                    가입된 이메일과 비밀번호가 필요합니다.<br>
                    소셜 로그인 계정은 이 API를 사용할 수 없습니다.<br>

                    **[성공]**

                    사용자 기본 정보와 함께 토큰이 발급됩니다.<br>
                    Access Token → 응답 Body에 포함
                    Refresh Token → HttpOnly 쿠키로 전달

                    **[탈퇴 예정 계정]**

                    탈퇴 처리 중인 계정은 로그인이 제한됩니다.<br>
                    탈퇴 계정 복구 API를 통해 계정을 먼저 복구해야 합니다.

                    **[토큰 정보]**

                    | 토큰 | 전달 방식 | 용도 |
                    |------|-----------|------|
                    | Access Token | Response Body | API 인증 헤더에 사용 |
                    | Refresh Token | HttpOnly Cookie | 토큰 재발급 시 사용 |
                    """
    )
    @SecurityRequirements(value = {})
    ApiResponse<AuthResDto.LoginResponseDto> login(
            @RequestBody @Valid AuthReqDto.LoginDto request,
            HttpServletResponse response
    );

    @Operation(
            summary = "토큰 재발급",
            description = """
                    쿠키에 저장된 Refresh Token을 사용하여 새로운 토큰을 재발급받습니다.<br>

                    **[요청 조건]**

                    쿠키에 유효한 refreshToken이 존재해야 합니다.<br>
                    만료되거나 무효화된 Refresh Token은 사용할 수 없습니다.<br>

                    **[RTR (Refresh Token Rotation) 방식]**

                    Access Token과 Refresh Token이 모두 새로 발급됩니다.
                    기존 Refresh Token은 즉시 무효화됩니다.<br>
                    새로운 Refresh Token은 HttpOnly 쿠키로 전달됩니다.<br>

                    **[보안 참고]**

                    하나의 Refresh Token은 단 한 번만 사용 가능합니다.<br>
                    이미 사용된 Refresh Token으로 재발급 시도 시 토큰 탈취로 간주되어 모든 토큰이 무효화될 수 있습니다.<br>
                    """
    )
    @SecurityRequirements(value = {})
    ApiResponse<AuthResDto.TokenDto> refresh(
            @Parameter(hidden = true)
            @CookieValue(name = "refreshToken", required = true) String refreshToken,
            HttpServletResponse response
    );

    @Operation(
            summary = "로그아웃",
            description = """
                    현재 로그인된 사용자를 로그아웃 처리합니다.<br>

                    **[인증 필요]**

                    Header에 유효한 Access Token이 필요합니다.<br>
                    Authorization: Bearer {accessToken}<br>

                    **[처리 내용]**

                    Redis에 저장된 해당 사용자의 Refresh Token 삭제<br>
                    쿠키에 저장된 Refresh Token 제거<br>

                    **[주의]**

                    로그아웃 후 기존 Access Token은 만료 시까지 유효할 수 있습니다.<br>
                    Refresh Token은 즉시 무효화되므로 토큰 재발급이 불가합니다.<br>
                    """
    )
    ApiResponse<String> logout(@AuthenticationPrincipal UserDetails userDetails, HttpServletResponse response);

    @Operation(
            summary = "소셜 로그인 토큰 교환",
            description = """
                    소셜 로그인(OAuth2) 인증 후 전달받은 Authorization Code를 서비스 토큰으로 교환합니다.<br>

                    **[요청 조건]**

                    소셜 로그인 Redirect URL에서 전달받은 code 파라미터가 필요합니다.<br>
                    유효한 Authorization Code여야 합니다.<br>

                    **[처리 흐름]**

                    ① Authorization Code로 소셜 서비스에서 사용자 정보를 조회합니다.<br>

                    ② 신규 사용자 → 자동으로 회원가입이 진행된 후 로그인됩니다.<br>

                    ③ 기존 사용자 → 바로 로그인 처리됩니다.<br>

                    **[성공]**

                    사용자 기본 정보와 함께 토큰이 발급됩니다.<br>
                    Access Token → 응답 Body에 포함
                    Refresh Token → HttpOnly 쿠키로 전달

                    **[토큰 정보]**

                    | 토큰 | 전달 방식 | 용도 |
                    |------|-----------|------|
                    | Access Token | Response Body | API 인증 헤더에 사용 |
                    | Refresh Token | HttpOnly Cookie | 토큰 재발급 시 사용 |
                    """
    )
    @SecurityRequirements(value = {})
    ApiResponse<AuthResDto.LoginResponseDto> exchangeOAuthToken(
            @RequestBody @Valid AuthReqDto.OAuthLoginReqDto request,
            HttpServletResponse response
    );

    @Operation(
            summary = "탈퇴 계정 복구",
            description = """
                    삭제 요청된 계정을 복구합니다.<br>

                    **[요청 조건]**

                    탈퇴 처리 중인 계정의 이메일과 비밀번호가 필요합니다.<br>
                    이미 완전히 삭제된 계정은 복구할 수 없습니다.<br>

                    **[성공]**

                    탈퇴 예정 상태가 취소됩니다.
                    정상적으로 로그인 처리되어 토큰이 발급됩니다.<br>
                    Access Token → 응답 Body에 포함
                    Refresh Token → HttpOnly 쿠키로 전달

                    **[주의]**

                    탈퇴 유예 기간 내에만 복구가 가능합니다.<br>
                    복구 후에는 기존 데이터가 그대로 유지됩니다.<br>
                    """
    )
    @SecurityRequirements(value = {})
    ApiResponse<AuthResDto.TokenDto> recover(
            @RequestBody @Valid AuthReqDto.LoginDto request,
            HttpServletResponse response
    );

    @Operation(
            summary = "비밀번호 재설정 인증번호 발송",
            description = """
                    비밀번호를 잊어버린 사용자를 위해 이메일로 6자리 인증번호를 발송합니다.<br>

                    **[요청 조건]**

                    가입 여부와 관계없이 동일한 응답이 반환됩니다.<br>
                    소셜 로그인 전용 계정은 비밀번호 재설정이 불가합니다.<br>

                    **[유효기간]**

                    발송 후 3분간 유효합니다.<br>
                    유효기간 만료 시 재발송이 필요합니다.<br>

                    **[처리 흐름]**

                    ① 이메일로 인증번호가 발송됩니다.<br>

                    ② 비밀번호 재설정 인증번호 검증 API를 호출하여 인증합니다.<br>

                    ③ 검증 성공 시 발급된 resetToken으로 비밀번호 재설정 API를 호출합니다.<br>
                    """
    )
    @SecurityRequirements(value = {})
    ApiResponse<Void> sendPasswordResetCode(
            @RequestBody @Valid AuthReqDto.EmailSendDto request,
            HttpServletRequest httpServletRequest
    );

    @Operation(
            summary = "비밀번호 재설정 인증번호 검증",
            description = """
                    이메일로 발송된 6자리 인증번호를 검증합니다.<br>

                    **[요청 조건]**

                    비밀번호 재설정 인증번호 발송 API를 먼저 호출해야 합니다.<br>
                    발송된 인증번호와 정확히 일치해야 합니다.<br>

                    **[성공]**

                    비밀번호 재설정에 사용할 일회용 토큰(resetToken)이 발급됩니다.<br>
                    이 토큰은 5분간 유효합니다.<br>
                    토큰은 한 번만 사용 가능합니다.<br>

                    **[이후 처리]**

                    발급받은 resetToken을 비밀번호 재설정 API에 전달하여 비밀번호를 변경합니다.<br>

                    **[토큰 정보]**

                    | 항목 | 내용 |
                    |------|------|
                    | 토큰 이름 | resetToken |
                    | 유효기간 | 5분 |
                    | 사용 횟수 | 1회 (일회용) |
                    """
    )
    @SecurityRequirements(value = {})
    ApiResponse<AuthResDto.PasswordResetTokenDto> verifyPasswordResetCode(
            @RequestBody @Valid AuthReqDto.EmailVerifyDto request
    );

    @Operation(
            summary = "비밀번호 재설정",
            description = """
                    인증번호 검증으로 발급받은 resetToken을 사용하여 새로운 비밀번호로 재설정합니다.<br>

                    **[요청 조건]**

                    비밀번호 재설정 인증번호 검증 API에서 발급받은 resetToken이 필요합니다.<br>
                    새로운 비밀번호는 보안 정책에 맞게 설정해야 합니다.<br>

                    **[성공]**

                    비밀번호가 새로운 비밀번호로 변경됩니다.<br>
                    사용된 resetToken은 즉시 무효화됩니다.<br>
                    새 비밀번호로 일반 로그인 API를 통해 로그인할 수 있습니다.<br>

                    **[주의]**

                    resetToken이 만료되었거나 이미 사용된 경우 실패합니다.<br>
                    실패 시 인증번호 발송부터 다시 진행해야 합니다.<br>
                    """
    )
    @SecurityRequirements(value = {})
    ApiResponse<Void> resetPassword(
            @RequestBody @Valid AuthReqDto.PasswordResetDto request
    );

    @Operation(
            summary = "비밀번호 변경",
            description = """
                    로그인한 상태에서 현재 비밀번호를 새 비밀번호로 변경합니다.<br>

                    **[인증 필요]**

                    Header에 유효한 Access Token이 필요합니다.<br>
                    Authorization: Bearer {accessToken}<br>

                    **[요청 조건]**

                    로컬 로그인 회원(이메일 가입)만 사용 가능합니다.
                    소셜 로그인 회원은 이용할 수 없습니다.<br>
                    현재 비밀번호를 정확히 입력해야 합니다.<br>
                    새 비밀번호는 보안 정책에 맞게 설정해야 합니다.<br>

                    **[성공]**

                    비밀번호가 즉시 변경됩니다.<br>
                    기존 세션은 유지되며, 다음 로그인부터 새 비밀번호를 사용합니다.<br>

                    **[주의]**

                    현재 비밀번호와 새 비밀번호가 동일할 수 없습니다.<br>
                    현재 비밀번호가 일치하지 않으면 변경이 거부됩니다.<br>
                    """
    )
    ApiResponse<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AuthReqDto.PasswordChange request
    );
}