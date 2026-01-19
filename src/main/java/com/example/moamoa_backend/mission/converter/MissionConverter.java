package com.example.moamoa_backend.mission.converter;

import com.example.moamoa_backend.mission.dto.request.MissionRequestDto;
import com.example.moamoa_backend.mission.dto.response.MissionResponseDto;
import com.example.moamoa_backend.mission.entity.Mission;
import com.example.moamoa_backend.mission.exception.MissionException;
import com.example.moamoa_backend.mission.exception.code.MissionErrorCode;
import com.example.moamoa_backend.quiz.entity.Quiz;
import com.example.moamoa_backend.quiz.enums.QuizType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MissionConverter {

    private final ObjectMapper objectMapper;

    public MissionResponseDto.CreateResult toCreateResult(Mission mission){
        return MissionResponseDto.CreateResult.builder()
                .missionId(mission.getId())
                .totalReward(mission.getReward())
                .totalDutation(mission.getDurationMinutes())
                .createAt(mission.getCreatedAt())
                .build();
    }

    public Mission toEntity(MissionRequestDto.Create request){
        List<Quiz> quizzes = request.quizzes().stream()
                .map(this::toQuizEntity)
                .collect(Collectors.toList());

        int totalReward = quizzes.stream().mapToInt(quiz-> getRewardByType(quiz.getType()))
                .sum();

        int totalTimeSeconds = request.videoLength();
        for(Quiz quiz: quizzes){
            totalTimeSeconds += getTimeByType(quiz.getType());
        }

        int durationMinutes = (int)Math.ceil(totalTimeSeconds/60.0);

        Mission mission = Mission.builder()
                .title(request.title())
                .description(request.description())
                .videoUrl(request.videoUrl())
                .durationMinutes(durationMinutes)
                .videoLength(request.videoLength())
                .reward(totalReward)
                .build();

        quizzes.forEach(quiz -> quiz.setMission(mission));
        return mission;
    }

    private Quiz toQuizEntity(MissionRequestDto.CreateQuiz request){
        try{
            String optionsJson = objectMapper.writeValueAsString(request.options());
            return Quiz.builder()
                    .question(request.question())
                    .type(request.type())
                    .answer(request.answer())
                    .detailInformation(optionsJson)
                    .build();
        }catch(Exception e){
            throw new MissionException(MissionErrorCode.QUIZ_JSON_CONVERSION_FAIL);
        }
    }
    private int getRewardByType(QuizType type){
        return switch (type){
            case OX -> 3;
            case MULTIPLE -> 5;
            case SHORT -> 10;
        };
    }

    private int getTimeByType(QuizType type){
        return switch (type){
            case OX -> 30;
            case MULTIPLE -> 60;
            case SHORT -> 80;
        };

    }
}
