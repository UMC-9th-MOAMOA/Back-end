package com.example.moamoa_backend.mission.converter;

import com.example.moamoa_backend.domain.keyword.entity.Keyword;
import com.example.moamoa_backend.domain.member.entity.Member;
import com.example.moamoa_backend.domain.member.entity.mapping.MemberMission;
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
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    public Mission toEntity(MissionRequestDto.Create request, int videoDuration){
        List<Quiz> quizzes = request.quizzes().stream()
                .peek(this::validateQuizAnswer)
                .map(this::toQuizEntity)
                .collect(Collectors.toList());

        int totalReward = quizzes.stream().mapToInt(quiz-> getRewardByType(quiz.getType()))
                .sum();

        int totalTimeSeconds = videoDuration;
        for(Quiz quiz: quizzes){
            totalTimeSeconds += getTimeByType(quiz.getType());
        }

        int durationMinutes = (int)Math.ceil(totalTimeSeconds/60.0);

        Mission mission = Mission.builder()
                .title(request.title())
                .videoUrl(request.videoUrl())
                .durationMinutes(durationMinutes)
                .videoLength(videoDuration)
                .reward(totalReward)
                .build();

        quizzes.forEach(quiz -> quiz.setMission(mission));
        mission.getQuizzes().addAll(quizzes);
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
                .videoUrl(mission.getVideoUrl())
                .durationMinutes(mission.getDurationMinutes())
                .videoLength(mission.getVideoLength())
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

    //상태 변경 API용 메서드
    public MemberMission toMemberMission(Member member, Mission mission, MissionStatus missionStatus){

        //SCRAP이 아니면 무조건 도전(시도횟수 +1)로 간주
        int cnt = 0;
        if(missionStatus != MissionStatus.SCRAP){
            cnt = 1;
        }

        return MemberMission.builder()
                .member(member)
                .mission(mission)
                .missionStatus(missionStatus)
                .attemptCount(cnt)
                .isContentWatched(false)
                .build();
    }

    public MissionResponseDto.StatusResult toStatusResult(MemberMission memberMission) {
        return MissionResponseDto.StatusResult.builder()
                .missionId(memberMission.getMission().getId())
                .status(memberMission.getMissionStatus().name())
                .attemptCount(memberMission.getAttemptCount())
                .build();
    }

    public MissionResponseDto.WatchResult toWatchResult(MemberMission memberMission) {
        return MissionResponseDto.WatchResult.builder()
                .missionId(memberMission.getMission().getId())
                .isContentWatched(memberMission.isContentWatched())
                .status(memberMission.getMissionStatus().name())
                .build();
    }

    public MissionResponseDto.KeywordDto toKeywordDto(Keyword keyword){
        return MissionResponseDto.KeywordDto.builder()
                .keywordId(keyword.getId())
                .name(keyword.getName())
                .type(keyword.getKeywordType().name())
                .build();
    }

    public MissionResponseDto.KeywordListResult toKeywordList(List<Keyword> keywords){
        List<MissionResponseDto.KeywordDto> keywordDtos = keywords.stream()
                .map(this::toKeywordDto)
                .toList();

        return MissionResponseDto.KeywordListResult.builder()
                .keywords(keywordDtos)
                .build();
    }

    public MissionResponseDto.RecommendResult toRecommendResult(Mission mission,boolean isScrapped){
        List<String> keywords = mission.getMissionKeywords().stream()
                .map(mk -> mk.getKeyword().getName())
                .toList();

        String categoryName = "";
        if(!mission.getMissionSubInterests().isEmpty()){
            categoryName = mission.getMissionSubInterests().get(0)
                    .getSubInterest()
                    .getInterest()
                    .getName();
        }

        return MissionResponseDto.RecommendResult.builder()
                .missionId(mission.getId())
                .title(mission.getTitle())
                .durationMinutes(mission.getDurationMinutes())
                .category(categoryName)
                .quizCount(mission.getQuizzes().size())
                .keywords(keywords)
                .isScrapped(isScrapped)
                .videoUrl(mission.getVideoUrl())
                .build();
    }

    public MissionResponseDto.SearchResponse toSearchResponse(Slice<Mission> missionSlice, Map<Long, MemberMission> myMissionMap){
        List<MissionResponseDto.RecommendResult> missonDtos = missionSlice.getContent().stream()
                .map(mission -> {
                    MemberMission mm = myMissionMap.get(mission.getId());

                    boolean isScrapped = (mm!=null && mm.getMissionStatus() == MissionStatus.SCRAP);

                    return toRecommendResult(mission,isScrapped);
                })
                .collect(Collectors.toList());

        return MissionResponseDto.SearchResponse.builder()
                .missions(missonDtos)
                .hasNext(missionSlice.hasNext())
                .build();
    }

    public MissionResponseDto.SearchResponse toMyMissionsResult(
            Slice<MissionResponseDto.RecommendResult> slice,
            Map<Long, List<String>> keywordMap
    ) {
        List<MissionResponseDto.RecommendResult> newContent = slice.getContent().stream()
                .map(dto -> MissionResponseDto.RecommendResult.builder()
                        .missionId(dto.missionId())
                        .title(dto.title())
                        .durationMinutes(dto.durationMinutes())
                        .category(dto.category())
                        .quizCount(dto.quizCount())
                        .isScrapped(dto.isScrapped())
                        .videoUrl(dto.videoUrl())
                        .keywords(keywordMap.getOrDefault(dto.missionId(), Collections.emptyList())) // 🔑 키워드 주입
                        .build())
                .toList();

        return MissionResponseDto.SearchResponse.builder()
                .missions(newContent)
                .hasNext(slice.hasNext())
                .build();
    }

    public MissionResponseDto.SubmitResult toSubmitResult(
       boolean isSuccess,
       int missionReward,
       int goalReward,
       int dailyCount,
       boolean dailyGoalAchieved,
       int weeklyCount,
       boolean weeklyGoalAchieved
    ){
        return MissionResponseDto.SubmitResult.builder()
                .isSuccess(isSuccess)
                .missionReward(missionReward)
                .goalReward(goalReward)
                .totalReward(missionReward + goalReward)
                .dailyCount(dailyCount)
                .dailyGoalAchieved(dailyGoalAchieved)
                .weeklyCount(weeklyCount)
                .weeklyGoalAchieved(weeklyGoalAchieved)
                .build();
    }

    private void validateQuizAnswer(MissionRequestDto.CreateQuiz request) {
        if (request.type() == QuizType.MULTIPLE) {
            try {
                // "1" -> 1 변환 시도
                int answerIndex = Integer.parseInt(request.answer().trim());
                int optionSize = request.options().size();

                // 범위 체크 (1번 ~ 보기 개수)
                if (answerIndex < 1 || answerIndex > optionSize) {
                    throw new MissionException(MissionErrorCode.INVALID_QUIZ_ANSWER);
                }
            } catch (NumberFormatException e) {
                // 숫자가 아닌 값이 들어옴
                throw new MissionException(MissionErrorCode.INVALID_QUIZ_ANSWER);
            }
        }
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

        List<String> acceptedAnswers = Collections.emptyList();
        if (quiz.getAnswer() != null) {
            acceptedAnswers = List.of(quiz.getAnswer().split(",")).stream()
                    .map(String::trim)
                    .toList();
        }

        return MissionResponseDto.QuizDetail.builder()
                .quizId(quiz.getId())
                .type(quiz.getType().toString())
                .question(quiz.getQuestion())
                .option(options)
                .explanation(quiz.getExplanation())
                .answer(quiz.getAnswer())
                .acceptedAnswers(acceptedAnswers)
                .build();
    }

    private Quiz toQuizEntity(MissionRequestDto.CreateQuiz request){
        try{
            String optionsJson = objectMapper.writeValueAsString(request.options());
            return Quiz.builder()
                    .question(request.question())
                    .type(request.type())
                    .answer(request.answer().trim())
                    .detailInformation(optionsJson)
                    .explanation(request.explanation())
                    .build();
        }catch(Exception e){
            throw new MissionException(MissionErrorCode.QUIZ_JSON_CONVERSION_FAIL);
        }
    }


    public int getRewardByType(QuizType type){
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
