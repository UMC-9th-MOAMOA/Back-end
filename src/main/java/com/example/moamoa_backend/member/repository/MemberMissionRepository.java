package com.example.moamoa_backend.member.repository;

import com.example.moamoa_backend.member.entity.mapping.MemberMission;
import com.example.moamoa_backend.mission.enums.MissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberMissionRepository extends JpaRepository<MemberMission, Long> {
    Optional<MemberMission> findByMemberIdAndMissionId(Long memberId, Long missionId);

    boolean existsByMemberIdAndMissionIdAndMissionStatus(Long memberId, Long missionId, MissionStatus status);
}
