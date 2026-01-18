package com.example.moamoa_backend.auth.dto.req;

import com.example.moamoa_backend.member.entity.Member;
import com.example.moamoa_backend.member.enums.Role;
import com.example.moamoa_backend.member.enums.Provider;
import com.example.moamoa_backend.member.enums.MemberStatus;
import com.example.moamoa_backend.member.enums.Gender;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

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
            @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*?_]).{8,64}$",
                    message = "비밀번호는 8~64자 영문, 숫자, 특수문자를 모두 포함해야 합니다.")
            String password,

            @NotBlank(message = "이름은 필수 입력 값입니다.")
            String name,

            @NotNull(message = "생년월일은 필수 입력 값입니다.")
            LocalDate birth,

            @NotNull(message = "성별은 필수 입력 값입니다.")
            Gender gender,

            @NotNull(message = "약관 동의 내역은 필수입니다.")
            @Size(min = 1, message = "최소 하나 이상의 약관에 동의해야 합니다.")
            List<TermDto> agreedTerms
    ) {
        // 엔티티 변환 메서드
        public Member toEntity(PasswordEncoder passwordEncoder) {
            return Member.builder()
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .name(name)
                    .birthday(birth)
                    .gender(gender)
                    .role(Role.ROLE_USER)
                    .provider(Provider.LOCAL)
                    .status(MemberStatus.ACTIVE)
                    .build();
        }
    }

    // 약관 동의 상세
    public record TermDto(
            @NotNull
            Long policyId,

            @NotNull
            Boolean agreed
    ) {}

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

}