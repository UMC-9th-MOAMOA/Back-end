package com.example.moamoa_backend.domain.member.repository;

import com.example.moamoa_backend.domain.member.entity.Member;
import com.example.moamoa_backend.domain.member.entity.mapping.MemberMission;
import com.example.moamoa_backend.domain.mission.entity.Mission;
import com.example.moamoa_backend.domain.mission.enums.MissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberMissionRepository extends JpaRepository<MemberMission, Long> {
    Optional<MemberMission> findByMemberIdAndMissionId(Long memberId, Long missionId);

    boolean existsByMemberIdAndMissionIdAndMissionStatus(Long memberId, Long missionId, MissionStatus status);

    List<MemberMission> findAllByMemberIdAndMissionIdIn(Long memberId, List<Long> missionIds);

}
