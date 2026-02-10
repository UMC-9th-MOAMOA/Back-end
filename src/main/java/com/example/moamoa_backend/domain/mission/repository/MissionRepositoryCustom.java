package com.example.moamoa_backend.domain.mission.repository;

import com.example.moamoa_backend.domain.mission.dto.response.MissionResponseDto;
import com.example.moamoa_backend.domain.mission.entity.Mission;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Optional;

public interface MissionRepositoryCustom {
    List<Mission> findTodayRecommendMission(Long memberId, List<Long> interestIds, Integer time);

    Slice<Mission> searchMissions(
            Long memberId,
            String searchText, //검색어임. 없으면 null
            List<String> keywords, //키워드. 없으면 null
            Long categoryId, //대분류 ID. 없으면 null
            Long subCategoryId, //소분류 ID. 없으면 null
            Long seed, //랜덤 시드(무작위 정렬을 위함)
            Pageable pageable
    );

    Slice<MissionResponseDto.RecommendResult> getMyMissions(
            Long memberId, String status, String condition, Long categoryId, Pageable pageable
    );

    Optional<Mission> findByIdWithDetail(Long missionId);
}
