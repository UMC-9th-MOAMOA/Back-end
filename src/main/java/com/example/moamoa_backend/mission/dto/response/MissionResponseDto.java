package com.example.moamoa_backend.mission.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "미션 상세 조회 응답 DTO")
    public record MissionDetail(
            @Schema(description = "미션 ID", example = "1")
            Long missionId,

            @Schema(description = "미션 제목", example = "주식 차트 보는 법 A to Z")
            String title,

            @Schema(description = "미션 카테고리 (대분류)", example = "경제")
            String interest,

            @Schema(description = "유튜브 영상 URL", example = "https://youtu.be/...")
            String videoUrl,

            @Schema(description = "영상 소요 시간 (분)", example = "15")
            int durationMinutes,

            @Schema(description = "총 보상 (도토리)", example = "50")
            int totalReward,

            @Schema(description = "미션 키워드 리스트", example = "[\"초보\", \"재테크\"]")
            List<String> keyword,

            @Schema(description = "퀴즈 리스트")
            List<QuizDetail> quizzes,

            @Schema(description = "영상 시청 완료 여부 (true면 도전 가능)", example = "false")
            boolean isContentWatched,

            @Schema(description = "현재까지 시도한 횟수", example = "0")
            int attemptCount,

            @Schema(description = "보상 획득 일시", nullable = true)
            LocalDateTime rewardAt
    ){}

    @Builder
    @Schema(description = "퀴즈 상세 정보")
    public record QuizDetail(
            @Schema(description = "퀴즈 ID")
            Long quizId,

            @Schema(description = "퀴즈 타입 (OX, MULTIPLE, SHORT)")
            String type,

            @Schema(description = "질문 내용")
            String question,

            @Schema(description = "보기 리스트 (OX=['O','X'], 객관식=['보기1','보기2'], 단답형=[])")
            List<String> option,

            @Schema(description = "해설")
            String explanation,

            @Schema(description = "정답 (DB 원본 - 단순 참고용)", example = "클럭, Clock")
            String answer,

            @Schema(description = "인정 가능한 정답 리스트 (프론트 채점용 - 여기 포함되면 정답 처리)",
                    example = "[\"클럭\", \"Clock\"]")
            List<String> acceptedAnswers
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
