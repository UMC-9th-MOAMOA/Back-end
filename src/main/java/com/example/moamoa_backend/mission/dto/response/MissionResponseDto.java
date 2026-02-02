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
       String description,
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

    @Builder
    public record SubmitResult(
       boolean isSuccess, //이번 미션 성공 여부
       int missionReward, //이번 미션 보상(첫 성공 아니면 0)
       int goalReward, //목표 달성 추가 보상
       int totalReward, //총 획득량
       int dailyCount, //오늘 성공 횟수
       boolean dailyGoalAchieved, //일간 목표 달성 여부
       int weeklyCount, //이번주 성공 횟수
       boolean weeklyGoalAchieved //주간 목표 달성 여부
    ){}
}
