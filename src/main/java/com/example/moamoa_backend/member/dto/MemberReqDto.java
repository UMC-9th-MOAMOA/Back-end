package com.example.moamoa_backend.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class MemberReqDto {

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
