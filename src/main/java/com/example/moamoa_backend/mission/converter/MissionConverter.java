package com.example.moamoa_backend.mission.converter;

import com.example.moamoa_backend.keyword.entity.Keyword;
import com.example.moamoa_backend.member.entity.Member;
import com.example.moamoa_backend.member.entity.mapping.MemberMission;
import com.example.moamoa_backend.mission.dto.request.MissionRequestDto;
import com.example.moamoa_backend.mission.dto.response.MissionResponseDto;
import com.example.moamoa_backend.mission.entity.Mission;
import com.example.moamoa_backend.mission.entity.mapping.MissionSubInterest;
import com.example.moamoa_backend.mission.enums.MissionStatus;
import com.example.moamoa_backend.mission.exception.MissionException;
import com.example.moamoa_backend.mission.exception.code.MissionErrorCode;
import com.example.moamoa_backend.quiz.entity.Quiz;
import com.example.moamoa_backend.quiz.enums.QuizType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
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
                .totalDuration(mission.getDurationMinutes())
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

    public MissionResponseDto.MissionDetail toMissionDetail(Mission mission, MemberMission memberMission){
        String interestName = "";
        if(!mission.getMissionSubInterests().isEmpty()){
            MissionSubInterest link = mission.getMissionSubInterests().get(0);
            interestName = link.getSubInterest().getInterest().getName();
        }
        List<String> keywords= mission.getMissionKeywords().stream().map(mk -> mk.getKeyword().getName()).collect(Collectors.toList());

        List<MissionResponseDto.QuizDetail> quizDtos = mission.getQuizzes().stream().map(quiz->this.toQuizDetail(quiz)).collect(Collectors.toList());
        boolean isWatched = (memberMission!=null) && memberMission.isContentWatched();
        int attemptCount = (memberMission!=null) ? memberMission.getAttemptCount() : 0;
        LocalDateTime rewardAt = (memberMission!=null) ? memberMission.getRewardAt() : null;

        return MissionResponseDto.MissionDetail.builder()
                .missionId(mission.getId())
                .title(mission.getTitle())
                .description(mission.getDescription())
                .videoUrl(mission.getVideoUrl())
                .durationMinutes(mission.getDurationMinutes())
                .interest(interestName)
                .totalReward(mission.getReward())
                .keyword(keywords)
                .quizzes(quizDtos)
                .isContentWatched(isWatched)
                .attemptCount(attemptCount)
                .rewardAt(rewardAt)
                .build();
    }

    //영상 시청 완료 상태의 MemberMission 엔티티 생성
    public MemberMission toMemberMission(Member member, Mission mission){
        return MemberMission.builder()
                .member(member)
                .mission(mission)
                .missionStatus(MissionStatus.NONE)
                .attemptCount(0)
                .isContentWatched(true)
                .build();
    }

    private MissionResponseDto.QuizDetail toQuizDetail(Quiz quiz){
        List<String> options = Collections.emptyList();

        if(quiz.getDetailInformation()!=null && !quiz.getDetailInformation().isEmpty()){
            try{
                options = objectMapper.readValue(quiz.getDetailInformation(), new TypeReference<List<String>>(){});
            }catch(JsonProcessingException e){
                throw new MissionException(MissionErrorCode.QUIZ_JSON_CONVERSION_FAIL);
            }
        }

        return MissionResponseDto.QuizDetail.builder()
                .quizId(quiz.getId())
                .type(quiz.getType().toString())
                .question(quiz.getQuestion())
                .option(options)
                .build();
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
