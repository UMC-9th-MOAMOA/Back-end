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
    QUIZ_JSON_CONVERSION_FAIL(HttpStatus.BAD_REQUEST, "MISSION400_1", "퀴즈 데이터 JSON 변환 실패. 형식을 맞춰주세요."),
    INVALID_MISSION_STATUS(HttpStatus.BAD_REQUEST, "MISSION400_2", "잘못된 미션 상태 변경 요청입니다."),
    MISSION_NOT_STARTED(HttpStatus.BAD_REQUEST, "MISSION400_3", "아직 미션을 시작하지 않았습니다. 도전하기를 먼저 눌러주세요."),
    INVALID_MISSION_STATUS_PARAM(HttpStatus.BAD_REQUEST, "MISSION400_4", "유효하지 않은 미션 상태(status) 파라미터입니다."),
    INVALID_YOUTUBE_URL(HttpStatus.BAD_REQUEST, "MISSION400_5", "유효하지 않은 유튜브 URL입니다."),
    YOUTUBE_VIDEO_NOT_FOUND(HttpStatus.NOT_FOUND, "MISSION404_4", "해당 유튜브 영상을 찾을 수 없습니다. (삭제되었거나 비공개)"),
    YOUTUBE_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "MISSION500_1", "유튜브 API 연동 중 오류가 발생했습니다."),
    INVALID_KEYWORD_TYPE(HttpStatus.BAD_REQUEST, "MISSION400_6", "유효하지 않은 키워드 타입입니다. (KEYWORD, SITUATION, SKILL 중 하나여야 합니다.)"),
    MISSION_VIDEO_NOT_WATCHED(HttpStatus.BAD_REQUEST, "MISSION400_7", "영상을 끝까지 시청해야 도전할 수 있습니다.");
    private final HttpStatus status;
    private final String code;
    private final String message;
}
