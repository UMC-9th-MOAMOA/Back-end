package com.example.moamoa_backend.domain.mission.enums;

public enum MissionStatus {
    NONE, //시작 전(탐색 리스트 노출)
    SCRAP, // 찜(찜 리스트 노출)
    SUCCESS, // 성공(완료 리스트 노출)
    FAIL //실패/포기 (재도전 리스트 노출, 탐색에서 숨김)
}
