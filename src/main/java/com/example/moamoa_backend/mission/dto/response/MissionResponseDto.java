package com.example.moamoa_backend.mission.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class MissionResponseDto {

    @Builder
    public record CreateResult(
       Long missionId,
       Integer totalReward,
       Integer totalDuration,
       LocalDateTime createAt
    ){}

    @Builder
    public record MissionDetail(
        Long missionId,
        String title,
        String description,
        String interest,
        String videoUrl,
        int durationMinutes,
        int totalReward,
        List<String> keyword,
        List<QuizDetail> quizzes,
        boolean isContentWatched,
        int attemptCount,
        LocalDateTime rewardAt

    ){}

    @Builder
    public record QuizDetail(
        Long quizId,
        String type,
        String question,
        List<String> option //["O","X"] or [] or ["객관식1","객관식2"]
    ){}

    @Builder
    public record StatusResult(
       Long missionId,
       String status,
       int attemptCount
    ){}

    @Builder
    public record WatchResult(
       Long missionId,
       boolean isContentWatched,
       String status
    ){}

    @Builder
    public record KeywordListResult(
            List<KeywordDto> keywords
    ){}

    @Builder
    public record KeywordDto(
        Long keywordId,
        String name,
        String type
    ){}

    @Builder
    public record RecommendResult(
       Long missionId,
       String title,
       int durationMinutes,
       String category, //대분류
       int quizCount,
       List<String> keywords,
       boolean isScrapped,
       String videoUrl
    ){}

    @Builder
    public record SearchResponse(
      List<RecommendResult> missions,
      boolean hasNext
    ){}
}
