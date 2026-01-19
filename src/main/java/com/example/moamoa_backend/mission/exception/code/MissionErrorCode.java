package com.example.moamoa_backend.mission.exception.code;

import com.example.moamoa_backend.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MissionErrorCode implements BaseErrorCode {

    MISSION_NOT_FOUND(HttpStatus.NOT_FOUND,"MISSION_404_1","해당 미션을 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "MISSION404_2", "해당 카테고리(SubInterest)를 찾을 수 없습니다."),
    KEYWORD_NOT_FOUND(HttpStatus.NOT_FOUND, "MISSION404_3", "해당 키워드를 찾을 수 없습니다."),
    QUIZ_JSON_CONVERSION_FAIL(HttpStatus.BAD_REQUEST, "MISSION400_1", "퀴즈 데이터 JSON 변환 실패. 형식을 맞춰주세요.");
    private final HttpStatus status;
    private final String code;
    private final String message;
}
