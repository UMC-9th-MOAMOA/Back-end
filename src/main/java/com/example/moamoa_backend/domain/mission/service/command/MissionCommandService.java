package com.example.moamoa_backend.domain.mission.service.command;

import com.example.moamoa_backend.domain.mission.dto.request.MissionRequestDto;
import com.example.moamoa_backend.domain.mission.dto.response.MissionResponseDto;

import org.springframework.transaction.annotation.Transactional;

public interface MissionCommandService {
	@Transactional
	MissionResponseDto.CreateResult createMission(MissionRequestDto.Create request);

	@Transactional
	MissionResponseDto.WatchResult updateMissionWatchStatus(Long memberId, Long missionId);

	@Transactional
	MissionResponseDto.StatusResult updateMissionStatus(Long memberId, Long missionId,
		MissionRequestDto.PatchStatus request);

	@Transactional
	MissionResponseDto.SubmitResult submitMissionAnswer(Long memberId, Long missionId,
		MissionRequestDto.SubmitAnswer request);
}
