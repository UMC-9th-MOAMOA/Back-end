package com.example.moamoa_backend.mission.service.query;

import com.example.moamoa_backend.mission.dto.response.MissionResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MissionQueryService {
    MissionResponseDto.MissionDetail getMissionDetail(Long memberId, Long missionId);

    MissionResponseDto.KeywordListResult getRecommendedKeywords();

    MissionResponseDto.KeywordListResult getRelatedKeywords(String keyword);

    List<MissionResponseDto.RecommendResult> getTodayRecommendMissions(Long memberId, Integer requestTime);

    MissionResponseDto.SearchResponse searchMissions(
            Long memberId, String searchText, List<String> keywords,
            Long categoryId, Long subCategoryId, Long seed, Pageable pageable
    );
}
