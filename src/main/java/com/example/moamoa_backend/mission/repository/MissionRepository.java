package com.example.moamoa_backend.mission.repository;

import com.example.moamoa_backend.mission.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRepository extends JpaRepository<Mission, Long>, MissionRepositoryCustom {
}
