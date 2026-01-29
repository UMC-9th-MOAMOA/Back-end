package com.example.moamoa_backend.member.repository;

import com.example.moamoa_backend.member.entity.MemberSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberSettingRepository extends JpaRepository<MemberSetting, Long> {

    Optional<MemberSetting> findByMemberIdAndSettingKey(Long memberId, String settingKey);
}