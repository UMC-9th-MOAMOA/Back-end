package com.example.moamoa_backend.member.dto.res;

import com.example.moamoa_backend.member.entity.Member;
import com.example.moamoa_backend.member.enums.Gender;

import java.time.LocalDate;

public class MemberResDto {

    public record ProfileResponse(
            Integer profileImage,
            String name,
            String email,
            LocalDate birthday,
            String gender,
            String phoneNumber,
            String provider
    ) {
        public static ProfileResponse from(Member member) {
            return new ProfileResponse(
                    member.getProfileImage(),
                    member.getName(),
                    member.getEmail(),
                    member.getBirthday(),
                    member.getGender() != null ? member.getGender().name() : null,
                    member.getPhoneNumber(),
                    member.getProvider().name()
            );
        }
    }
}
