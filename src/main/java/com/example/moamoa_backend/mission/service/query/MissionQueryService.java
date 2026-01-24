package com.example.moamoa_backend.mission.service.query;

import com.example.moamoa_backend.mission.dto.response.MissionResponseDto;

import java.util.List;

public interface MissionQueryService {
    MissionResponseDto.MissionDetail getMissionDetail(Long memberId, Long missionId);

    MissionResponseDto.KeywordListResult getRecommendedKeywords();

    MissionResponseDto.KeywordListResult getRelatedKeywords(String keyword);

    List<MissionResponseDto.RecommendResult> getTodayRecommendMissions(Long memberId, Integer requestTime);
}
