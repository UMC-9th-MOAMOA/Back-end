package com.example.moamoa_backend.domain.mission.service.query;

import com.example.moamoa_backend.domain.mission.dto.response.MissionResponseDto;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MissionQueryService {
	MissionResponseDto.MissionDetail getMissionDetail(Long memberId, Long missionId);

	MissionResponseDto.KeywordListResult getRecommendedKeywords();

	MissionResponseDto.KeywordListResult getRelatedKeywords(String keyword);

	List<MissionResponseDto.RecommendResult> getTodayRecommendMissions(Long memberId, Integer requestTime, Boolean isRefresh);

	MissionResponseDto.SearchResponse searchMissions(
		Long memberId, String searchText, List<String> keywords, Long seed, Pageable pageable
	);

	MissionResponseDto.SearchResponse getMissionsByCategory(
		Long memberId, Long categoryId, Long subCategoryId, Long seed, Pageable pageable
	);

	MissionResponseDto.SearchResponse getMyMissions(Long memberId, String status, String condition, Long categoryId,
		Pageable pageable);
}
