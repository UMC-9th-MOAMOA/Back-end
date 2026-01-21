package com.example.moamoa_backend.mission.service.query;

import com.example.moamoa_backend.mission.dto.response.MissionResponseDto;

public interface MissionQueryService {
    MissionResponseDto.MissionDetail getMissionDetail(Long memberId, Long missionId);

    MissionResponseDto.KeywordListResult getRecommendedKeywords();
}
