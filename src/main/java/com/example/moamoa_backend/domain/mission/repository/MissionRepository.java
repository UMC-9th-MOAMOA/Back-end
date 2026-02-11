package com.example.moamoa_backend.domain.mission.repository;

import com.example.moamoa_backend.domain.mission.entity.Mission;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRepository extends JpaRepository<Mission, Long>, MissionRepositoryCustom {
}
