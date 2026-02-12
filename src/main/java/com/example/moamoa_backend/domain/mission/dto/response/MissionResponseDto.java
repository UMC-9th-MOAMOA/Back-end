package com.example.moamoa_backend.domain.mission.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class MissionResponseDto {

	@Builder
	@Schema(description = "미션 생성 성공 응답 DTO")
	public record CreateResult(
		@Schema(description = "생성된 미션 ID", example = "1")
		Long missionId,

		@Schema(description = "총 보상 (도토리)", example = "30")
		Integer totalReward,

		@Schema(description = "영상 총 소요 시간(분)", example = "12")
		Integer totalDuration,

		@Schema(description = "생성 일시")
		LocalDateTime createAt
	) {
	}

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

		@Schema(description = "예상 소요 시간 (분)", example = "15")
		int durationMinutes,

		@Schema(description = "영상 총 길이 (초)", example = "890")
		int videoLength,

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
	) {
	}

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
	) {
	}

	@Builder
	@Schema(description = "미션 상태 변경 결과 DTO")
	public record StatusResult(
		@Schema(description = "미션 ID", example = "1")
		Long missionId,

		@Schema(description = "변경 후 상태 (NONE, SCRAP, FAIL)", example = "SCRAP")
		String status,

		@Schema(description = "현재 시도 횟수", example = "1")
		int attemptCount
	) {
	}

	@Builder
	@Schema(description = "미션 영상 시청 완료 결과 DTO")
	public record WatchResult(
		@Schema(description = "미션 ID", example = "1")
		Long missionId,

		@Schema(description = "시청 완료 여부 (항상 true)", example = "true")
		boolean isContentWatched,

		@Schema(description = "현재 미션 상태", example = "NONE")
		String status
	) {
	}

	@Builder
	@Schema(description = "키워드 리스트 조회 응답 DTO")
	public record KeywordListResult(
		@Schema(description = "키워드 목록")
		List<KeywordDto> keywords
	) {
	}

	@Builder
	@Schema(description = "키워드 정보 DTO")
	public record KeywordDto(
		@Schema(description = "키워드 ID", example = "10")
		Long keywordId,

		@Schema(description = "키워드 이름", example = "자투리시간")
		String name,

		@Schema(description = "키워드 타입 (SITUATION, SKILL, KEYWORD)", example = "SITUATION")
		String type
	) {
	}

	@Builder
	@Schema(description = "추천/검색 미션 요약 정보 DTO")
	public record RecommendResult(
		@Schema(description = "미션 ID", example = "1")
		Long missionId,

		@Schema(description = "미션 제목", example = "초보자를 위한 주식 강의")
		String title,

		@Schema(description = "소요 시간 (분)", example = "10")
		int durationMinutes,

		@Schema(description = "카테고리 (대분류)", example = "경제")
		String category,

		@Schema(description = "포함된 퀴즈 개수", example = "3")
		int quizCount,

		@Schema(description = "관련 키워드 리스트", example = "[\"재테크\", \"주식\"]")
		List<String> keywords,

		@Schema(description = "유저의 찜 여부", example = "true")
		boolean isScrapped,

		@Schema(description = "영상 URL (썸네일 용도 등)", example = "https://youtu.be/...")
		String videoUrl
	) {
	}

	@Builder
	@Schema(description = "미션 검색/조회 페이징 응답 DTO")
	public record SearchResponse(
		@Schema(description = "미션 리스트")
		List<RecommendResult> missions,

		@Schema(description = "다음 페이지 존재 여부", example = "true")
		boolean hasNext

	) {
	}

	@Builder
	@Schema(description = "미션 정답 제출 결과 DTO")
	public record SubmitResult(
		@Schema(description = "채점 결과 (true: 성공/부분성공, false: 완전실패)", example = "true")
		boolean isSuccess,

		@Schema(description = "이번 미션 퀴즈 보상 (첫 성공 시 지급)", example = "10")
		int missionReward,

		@Schema(description = "목표 달성 추가 보상", example = "5")
		int goalReward,

		@Schema(description = "총 획득 도토리 (mission + goal)", example = "15")
		int totalReward,

		@Schema(description = "오늘 성공한 미션 횟수", example = "1")
		int dailyCount,

		@Schema(description = "일간 목표 달성 여부 (이번 제출로 달성했으면 true)", example = "false")
		boolean dailyGoalAchieved,

		@Schema(description = "이번 주 성공한 미션 횟수", example = "3")
		int weeklyCount,

		@Schema(description = "주간 목표 달성 여부", example = "false")
		boolean weeklyGoalAchieved

	) {
	}
}
