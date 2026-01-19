package com.example.moamoa_backend.mission.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

public class MissionResponseDto {

    @Builder
    public record CreateResult(
       Long missionId,
       Integer totalReward,
       Integer totalDutation,
       LocalDateTime createAt
    ){}
}
