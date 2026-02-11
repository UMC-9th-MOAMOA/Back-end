package com.example.moamoa_backend.domain.mission.repository;

import com.example.moamoa_backend.domain.mission.entity.mapping.MissionKeyword;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionKeywordRepository extends JpaRepository<MissionKeyword, Long> {
}
