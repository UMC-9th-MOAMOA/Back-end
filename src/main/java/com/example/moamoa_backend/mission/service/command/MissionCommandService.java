package com.example.moamoa_backend.mission.service.command;

import com.example.moamoa_backend.mission.dto.request.MissionRequestDto;
import com.example.moamoa_backend.mission.dto.response.MissionResponseDto;
import org.springframework.transaction.annotation.Transactional;

public interface MissionCommandService {
    @Transactional
    MissionResponseDto.CreateResult createMission(MissionRequestDto.Create request);
}
