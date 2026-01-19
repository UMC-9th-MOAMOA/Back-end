package com.example.moamoa_backend.mission.dto.request;

import com.example.moamoa_backend.quiz.enums.QuizType;
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
            @NotNull(message = "영상 길이를 입력해주세요.(초단위로)")
            Integer videoLength,
            List<String> keywords,
            String category,
            List<CreateQuiz> quizzes
    ){}

    public record CreateQuiz(
            @NotBlank String question,
            @NotNull QuizType type,
            @NotBlank String answer,
            List<String> options
            ){}
}
