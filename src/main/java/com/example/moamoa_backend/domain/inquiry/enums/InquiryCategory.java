package com.example.moamoa_backend.domain.inquiry.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InquiryCategory {
    REWARD("보상"),
    MISSION_QUIZ("미션 및 퀴즈"),
    SHOP_DECORATION("상점 및 꾸미기"),
    ACCOUNT("계정"),
    ETC("기타");

    private final String description;
}
