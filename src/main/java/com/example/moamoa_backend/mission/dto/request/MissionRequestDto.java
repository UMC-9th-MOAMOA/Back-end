package com.example.moamoa_backend.mission.dto.request;

import com.example.moamoa_backend.quiz.enums.QuizType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class MissionRequestDto {

    @Schema(description = "미션 생성 요청 DTO")
    public record Create(
            @Schema(description = "미션 제목", example = "개발자 면접 단골 질문 모음")
            @NotBlank(message="제목은 필수입니다.")
            String title,

            @Schema(description = "유튜브 영상 URL (공유 링크 또는 브라우저 링크)", example = "https://www.youtube.com/watch?v=LJDHYWewzUw")
            @NotBlank(message = "영상 URL은 필수입니다.")
            String videoUrl,

            @Schema(description = "미션 키워드 리스트")
            @Valid List<KeywordCreate> keywords,

            @Schema(description = "소분류 이름", example = "CS")
            @NotBlank(message = "카테고리는 필수입니다.")
            String category,

            @Schema(
                    description = "퀴즈 리스트 (최소 1개 이상). OX, 객관식, 단답형 혼합 가능",
                    example = """
                    [
                      {
                        "question": "HTTP 프로토콜은 상태를 유지하지 않는(Stateless) 특성이 있다.",
                        "type": "OX",
                        "answer": "O",
                        "options": ["O", "X"],
                        "explanation": "HTTP는 기본적으로 상태를 저장하지 않는 프로토콜입니다."
                      },
                      {
                        "question": "다음 중 관계형 데이터베이스(RDBMS)가 아닌 것은?",
                        "type": "MULTIPLE",
                        "answer": "3",
                        "options": ["MySQL", "Oracle", "MongoDB", "PostgreSQL"],
                        "explanation": "MongoDB는 대표적인 NoSQL 데이터베이스입니다."
                      }
                    ]
                    """
            )
            @NotNull(message = "퀴즈는 필수입니다.")
            @Size(min = 1, message = "퀴즈는 최소 1개 이상 등록해야 합니다.")
            @Valid List<CreateQuiz> quizzes
    ){}


    @Schema(description = "키워드 생성 요청 DTO")
    public record KeywordCreate(
            @Schema(description = "키워드 이름 (태그)", example = "취준생필독")
            @NotBlank(message = "키워드 이름은 필수입니다.")
            String name,

            @Schema(description = "키워드 타입 (SITUATION, SKILL, KEYWORD)", example = "SITUATION", defaultValue = "KEYWORD")
            @NotBlank(message = "키워드 타입은 필수입니다.")
            String type
    ){}

    @Schema(description = "퀴즈 생성 요청 DTO")
    public record CreateQuiz(
            @Schema(description = "퀴즈 질문", example = "HTTP 프로토콜은 상태를 유지하지 않는(Stateless) 특성이 있다.")
            @NotBlank(message = "퀴즈 질문은 필수입니다.")
            String question,

            @Schema(description = "퀴즈 타입 (OX, MULTIPLE, SHORT)", example = "OX")
            @NotNull(message ="퀴즈 타입은 필수입니다.")
            QuizType type,

            @Schema(
                    description = "퀴즈 정답 (OX: 'O'/'X', 객관식: '1' 처럼 번호(String), 단답형: 정답 텍스트)",
                    example = "1"
            )
            @NotBlank(message = "퀴즈 답은 필수입니다.")
            String answer,

            @Schema(
                    description = "객관식 보기 리스트. OX는 [\"O\", \"X\"], 단답형은 [] 빈 배열",
                    example = "[\"O\", \"X\"]"
            )
            List<String> options,

            @Schema(description = "퀴즈 해설/설명", example = "경제학에서 희소성이란...")
            String explanation
            ){}

    @Schema(description = "미션 상태 변경 요청 DTO")
    public record PatchStatus(
            @Schema(
                    description = "변경할 상태값 (NONE, SCRAP, FAIL 중 택 1)",
                    example = "NONE",
                    allowableValues = {"NONE", "SCRAP", "FAIL"}
            )
            @NotBlank(message = "상태값은 필수입니다.")
            String status
    ){}

    @Schema(description = "미션 정답 제출 요청 DTO")
    public record SubmitAnswer(
            @Schema(description = "제출할 답안 목록")
            @NotNull(message = "답안 목록은 필수입니다.")
            @Valid List<QuizSubmission> submissions
    ){}

    @Schema(description = "개별 퀴즈 답안 제출 DTO")
    public record QuizSubmission(
            @Schema(description = "퀴즈 ID", example = "10")
            @NotNull(message = "퀴즈 ID는 필수입니다.")
            Long quizId,

            @Schema(description = "사용자가 입력한 정답", example = "O")
            @NotBlank(message = "답안은 필수입니다.")
            String answer
    ){}
}
