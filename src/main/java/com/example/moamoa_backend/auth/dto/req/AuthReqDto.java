package com.example.moamoa_backend.auth.dto.req;

import com.example.moamoa_backend.member.entity.Member;
import com.example.moamoa_backend.member.enums.Role;
import com.example.moamoa_backend.member.enums.Provider;
import com.example.moamoa_backend.member.enums.MemberStatus;
import com.example.moamoa_backend.policy.dto.req.PolicyReqDto;

import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 인증 관련 Request DTO
 */
public class AuthReqDto {

    // 이메일 전송 요청
    public record EmailSendDto(
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            @NotBlank(message = "이메일은 필수 입력 값입니다.")
            String email
    ) {}

    // 이메일 인증 번호 검증
    public record EmailVerifyDto(
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            @NotBlank(message = "이메일은 필수 입력 값입니다.")
            String email,

            @NotBlank(message = "인증 번호는 필수 입력 값입니다.")
            String authCode
    ) {}

    // 회원가입 요청
    public record SignupDto(
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            @NotBlank(message = "이메일은 필수 입력 값입니다.")
            String email,

            @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
            @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*?_.])[a-zA-Z0-9!@#$%^&*?_.]{8,64}$",
                    message = "비밀번호는 8~64자 영문, 숫자, 특수문자(!@#$%^&*?_.)를 모두 포함해야 합니다.")
            String password,

            @NotBlank(message = "비밀번호 확인은 필수 입력 값입니다.")
            String passwordCheck,

            @NotBlank(message = "이름은 필수 입력 값입니다.")
            String name,

            @NotNull(message = "약관 동의 내역은 필수입니다.")
            @Size(min = 1, message = "최소 하나 이상의 약관에 동의해야 합니다.")
            @Valid
            List<PolicyReqDto.@NotNull @Valid AgreementDto> agreedTerms
    ) {
        // 엔티티 변환 메서드
        public Member toEntity(PasswordEncoder passwordEncoder) {
            return Member.builder()
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .name(name)
                    .birthday(null)
                    .gender(null)
                    .role(Role.ROLE_USER)
                    .provider(Provider.LOCAL)
                    .providerId(email)
                    .status(MemberStatus.ACTIVE)
                    .policyAgreed(true) // 로컬 회원가입은 회원가입시 필수 정책 동의에 대한 검증이 포함됨
                    .build();
        }
    }

    // 로그인 요청
    public record LoginDto(
            @NotBlank(message = "이메일은 필수 입력 값입니다.")
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            String email,

            @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
            String password
    ){}

    // 토큰 재발급 요청
    public record ReissueDto(
            @NotBlank(message = "Refresh Token은 필수 입력 값입니다.")
            String refreshToken
    ){}

    // 소셜 로그인 시 accessToken 요청
    public record OAuthLoginReqDto(
            @NotBlank (message = "인증 코드는 필수입니다.")
            String code
    ){}

    /**
     * 비밀번호 초기화 요청
     * 1. 이메일로 인증번호 전송
     * 2. 인증번호 검증
     * 3. 검증을 통해 발급받은 토큰과 함께 새 비밀번호 제출
     */
    public record PasswordResetDto(
            @NotBlank(message = "토큰은 필수 입력 값입니다.")
            String token,

            @NotBlank(message = "새 비밀번호는 필수 입력 값입니다.")
            @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*?_.])[a-zA-Z0-9!@#$%^&*?_.]{8,64}$",
                    message = "비밀번호는 8~64자 영문, 숫자, 특수문자(!@#$%^&*?_.)를 모두 포함해야 합니다.")
            String newPassword,

            @NotBlank(message = "새 비밀번호 확인은 필수 입력 값입니다.")
            String newPasswordCheck
    ){}

    /**
     * 비밀번호 변경 요청
     * @param currentPassword 현재 비밀번호
     * @param newPassword 새 비밀번호
     * @param newPasswordCheck 새 비밀번호 확인
     */
    public record PasswordChange(

            @NotBlank(message = "현재 비밀번호는 필수입니다.")
            String currentPassword,

            @NotBlank(message = "새 비밀번호는 필수 입력 값입니다.")
            @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*?_.])[a-zA-Z0-9!@#$%^&*?_.]{8,64}$",
                    message = "비밀번호는 8~64자 영문, 숫자, 특수문자(!@#$%^&*?_.)를 모두 포함해야 합니다.")
            String newPassword,

            @NotBlank(message = "새 비밀번호 확인은 필수 입력 값입니다.")
            String newPasswordCheck
    ){}
}