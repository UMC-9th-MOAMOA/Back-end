package com.example.moamoa_backend.domain.policy.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PolicyType {

    TERMS_OF_SERVICE("서비스 이용약관"),
    PRIVACY_POLICY("개인정보 처리방침"),
    PRIVACY_COLLECTION("개인정보 수집 및 이용 동의"),
    ELECTRONIC_FINANCIAL("전자금융거래 약관"),
    MARKETING("마케팅 정보 수신 동의"),
    THIRD_PARTY("제3자 정보 제공 동의");

    private final String description;
}
