package com.example.moamoa_backend.mission.enums;

public enum MissionStatus {
    NONE, //시작 전
    SCRAP, //찜함
    IN_PROGRESS, //진행 중(실패 or 풀다 맒)
    SUCCESS //완료(성공)
}
