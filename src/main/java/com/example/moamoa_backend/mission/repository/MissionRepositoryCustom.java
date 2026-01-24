package com.example.moamoa_backend.mission.repository;

import com.example.moamoa_backend.mission.entity.Mission;

import java.util.List;

public interface MissionRepositoryCustom {
    List<Mission> findTodayRecommendMission(Long memberId, List<Long> interestIds, Integer time);
}
