package com.example.moamoa_backend.member.dto.req;

import com.example.moamoa_backend.member.enums.SettingValue;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

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

    public record ProfileUpdate(
            @NotNull(message = "프로필 이미지는 필수입니다.")
            @Min(value = 1, message = "프로필 이미지는 1~3 사이 값이어야 합니다.")
            @Max(value = 3, message = "프로필 이미지는 1~3 사이 값이어야 합니다.")
            Integer profileImage,

            @NotBlank(message = "이름은 필수입니다.")
            @Size(min = 2, max = 50, message = "이름은 2~50자 사이여야 합니다.")
            String name,

            LocalDate birthday,

            String gender
    ) {}

    public record SettingRequest(
            @NotBlank(message = "설정 키는 필수입니다.")
            String settingKey
    ) {}

}
