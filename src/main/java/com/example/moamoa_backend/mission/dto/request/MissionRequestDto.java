package com.example.moamoa_backend.mission.dto.request;

import com.example.moamoa_backend.quiz.enums.QuizType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class MissionRequestDto {

    public record Create(
            @NotBlank(message="제목은 필수입니다.")
            String title,
            String description,
            @NotBlank(message = "영상 URL은 필수입니다.")
            String videoUrl,
            @Valid List<KeywordCreate> keywords,
            String category,
            @Valid List<CreateQuiz> quizzes
    ){}
    public record KeywordCreate(
            @NotBlank(message = "키워드 이름은 필수입니다.")
            String name,
            @NotBlank(message = "키워드 타입은 필수입니다.")
            String type
    ){}
    public record CreateQuiz(
            @NotBlank(message = "퀴즈 질문은 필수입니다.")
            String question,
            @NotNull(message ="퀴즈 타입은 필수입니다.")
            QuizType type,
            @NotBlank(message = "퀴즈 답은 필수입니다.")
            String answer,
            List<String> options
            ){}

    public record PatchStatus(
            @NotBlank(message = "상태값은 필수입니다.")
            String status
    ){}

    public record SubmitAnswer(
            @NotNull(message = "답안 목록은 필수입니다.")
            @Valid List<QuizSubmission> submissions
    ){}

    public record QuizSubmission(
            @NotNull(message = "퀴즈 ID는 필수입니다.")
            Long quizId,
            @NotBlank(message = "답안은 필수입니다.")
            String answer
    ){}
}
