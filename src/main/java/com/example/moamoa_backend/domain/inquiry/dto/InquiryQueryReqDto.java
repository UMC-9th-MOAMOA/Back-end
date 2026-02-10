package com.example.moamoa_backend.inquiry.dto;

import com.example.moamoa_backend.inquiry.enums.InquiryCategory;

public class InquiryQueryReqDto {

    public enum Period {
        P1M(1), P3M(3), P6M(6), P1Y(12);
        private final int months;
        Period(int months) { this.months = months; }
        public int months() { return months; }
    }

    public enum AnswerStatus {
        ALL, COMPLETED, PENDING
    }

    public record MyInquiryList(
            Period period,                 // 필수
            InquiryCategory category,       // 선택(null이면 전체)
            AnswerStatus answerStatus,      // 필수
            Integer size,                  // 선택(null이면 기본값)
            String cursorCreatedAt,        // 선택(ISO-8601)
            Long cursorId                  // 선택
    ) {}
}
