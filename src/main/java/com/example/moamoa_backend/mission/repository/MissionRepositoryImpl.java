package com.example.moamoa_backend.mission.repository;

import com.example.moamoa_backend.mission.dto.response.MissionResponseDto;
import com.example.moamoa_backend.mission.entity.Mission;
import com.example.moamoa_backend.mission.enums.MissionStatus;
import com.example.moamoa_backend.mission.exception.MissionException;
import com.example.moamoa_backend.mission.exception.code.MissionErrorCode;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Optional;

import static com.example.moamoa_backend.interest.entity.QSubInterest.subInterest;
import static com.example.moamoa_backend.keyword.entity.QKeyword.keyword;
import static com.example.moamoa_backend.member.entity.mapping.QMemberMission.memberMission;
import static com.example.moamoa_backend.mission.entity.QMission.mission;
import static com.example.moamoa_backend.mission.entity.mapping.QMissionKeyword.missionKeyword;
import static com.example.moamoa_backend.mission.entity.mapping.QMissionSubInterest.missionSubInterest;
import static com.example.moamoa_backend.quiz.entity.QQuiz.quiz;

@RequiredArgsConstructor
public class MissionRepositoryImpl implements MissionRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Mission> findTodayRecommendMission(Long memberId, List<Long> interestIds, Integer time) {



        return queryFactory
                .selectFrom(mission)
                .leftJoin(memberMission)
                .on(memberMission.mission.id.eq(mission.id)
                        .and(memberMission.member.id.eq(memberId)))
                .where(
                        mission.missionSubInterests.any().subInterest.interest.id.in(interestIds),

                        durationLoe(time),

                        isAvailableMission()
                )
                .orderBy(
                        orderByScrapFirst(),
                        orderByScrapDuration(),
                        orderByRandom(null)
                )
                .limit(5)
                .fetch();
    }

    @Override
    public Slice<Mission> searchMissions(
            Long memberId,
            String searchText, //검색어임. 없으면 null
            List<String> keywords, //키워드. 없으면 null
            Long categoryId, //대분류 ID. 없으면 null
            Long subCategoryId, //소분류 ID. 없으면 null
            Long seed, //랜덤 시드(무작위 정렬을 위함)
            Pageable pageable
    ){

        List<Mission> results = queryFactory
                .selectFrom(mission)
                .leftJoin(memberMission)
                .on(memberMission.mission.id.eq(mission.id)
                        .and(memberMission.member.id.eq(memberId))
                )
                .where(
                        containsSearchText(searchText),
                        inKeywords(keywords),
                        eqCategory(categoryId),
                        eqSubCategory(subCategoryId),
                        isAvailableMission()
                )
                .orderBy(
                        orderByScrapFirst(),
                        orderByScrapDuration(),
                        orderByRandom(seed)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize()+1)
                .fetch();

        boolean hasNext = false;
        if(results.size() > pageable.getPageSize()){
            results.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(results, pageable, hasNext);
    }

    @Override
    public Slice<MissionResponseDto.RecommendResult> getMyMissions(
            Long memberId, String status, String condition, Long categoryId, Pageable pageable
    ) {
        List<MissionResponseDto.RecommendResult> content = queryFactory
                .select(Projections.constructor(MissionResponseDto.RecommendResult.class,
                        mission.id,
                        mission.title,
                        mission.durationMinutes,
                        JPAExpressions.select(subInterest.interest.name)
                                .from(missionSubInterest)
                                .join(missionSubInterest.subInterest, subInterest)
                                .where(missionSubInterest.mission.eq(mission))
                                .orderBy(missionSubInterest.id.asc())
                                .limit(1),
                        mission.quizzes.size(),
                        Expressions.nullExpression(List.class),
                        memberMission.missionStatus.eq(MissionStatus.SCRAP),
                        mission.videoUrl
                ))
                .from(memberMission)
                .join(memberMission.mission, mission)
                .where(
                        memberMission.member.id.eq(memberId),
                        filterStatus(status),
                        eqCategory(categoryId)
                )
                .orderBy(orderByMyMission(condition))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public Optional<Mission> findByIdWithDetail(Long missionId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(mission)
                        .leftJoin(mission.missionKeywords, missionKeyword).fetchJoin()
                        .leftJoin(missionKeyword.keyword, keyword).fetchJoin()
                        .leftJoin(mission.missionSubInterests, missionSubInterest).fetchJoin()
                        .leftJoin(missionSubInterest.subInterest, subInterest).fetchJoin()
                        .leftJoin(mission.quizzes, quiz).fetchJoin()
                        .where(mission.id.eq(missionId))
                        .distinct()
                        .fetchOne()
        );
    }
    private BooleanExpression durationLoe(Integer time) {
        return time != null ? mission.durationMinutes.loe(time) : null;
    }

    //찜한 상태(SCRAP)를 맨 위로 올리는 정렬 조건 메서드
    private OrderSpecifier<Integer> orderByScrapFirst(){
        return new CaseBuilder()
                .when(memberMission.missionStatus.eq(MissionStatus.SCRAP)).then(1)
                .otherwise(2).asc();
    }

    //찜한 미션중에서 소요시간 짧은 순으로 정렬
    private OrderSpecifier<Integer> orderByScrapDuration(){
        return new CaseBuilder()
                .when(memberMission.missionStatus.eq(MissionStatus.SCRAP))
                .then(mission.durationMinutes)
                .otherwise((Integer) null)
                .asc();
    }

    //필터링 로직. 리스트에 노출 가능한 미션 상태인지 확인
    private BooleanExpression isAvailableMission(){
        return memberMission.missionStatus.isNull()
                .or(memberMission.missionStatus.eq(MissionStatus.SCRAP))
                .or(
                        memberMission.missionStatus.eq(MissionStatus.NONE)
                                .and(memberMission.attemptCount.eq(0))
                );
    }
    //랜덤 정렬(시드 있으면 고정 랜덤, 없으면 완전 랜덤임! -> 무한 스크롤 페이징 중복 방지)
    private OrderSpecifier<Double> orderByRandom(Long seed){
        if(seed !=null && seed!=0L){
            return Expressions.numberTemplate(Double.class, "function('rand',{0})", mission.id.add(seed)).asc();

        }
        return Expressions.numberTemplate(Double.class, "function('rand')").asc();
    }

    //제목 검색
    private BooleanExpression containsSearchText(String searchText){
        return (searchText == null || searchText.trim().isEmpty()) ? null
                : mission.title.containsIgnoreCase(searchText);
    }

    //키워드 검색
    private BooleanExpression inKeywords(List<String> keywords){
        return (keywords == null || keywords.isEmpty()) ? null
                : mission.missionKeywords.any().keyword.name.in(keywords);
    }

    //대분류 필터
    private BooleanExpression eqCategory(Long categoryId){
        return (categoryId ==null) ? null
                : mission.missionSubInterests.any().subInterest.interest.id.eq(categoryId);
    }
    //소분류 필터
    private BooleanExpression eqSubCategory(Long subCategoryId) {
        return (subCategoryId == null) ? null
                : mission.missionSubInterests.any().subInterest.id.eq(subCategoryId);
    }

    //최근 저장순/소요시간순
    private OrderSpecifier<?> orderByMyMission(String condition) {
        if (condition == null || condition.trim().isEmpty()) {
            return memberMission.updatedAt.desc();
        }

        return switch (condition.trim().toUpperCase()) {
            case "TIME_ASC" -> mission.durationMinutes.asc();
            case "TIME_DESC" -> mission.durationMinutes.desc();
            default -> memberMission.updatedAt.desc();
        };
    }

    //상태 필터링
    private BooleanExpression filterStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new MissionException(MissionErrorCode.INVALID_MISSION_STATUS_PARAM);
        }
        return switch (status.toUpperCase()) {
            case "SCRAP" -> memberMission.missionStatus.eq(MissionStatus.SCRAP);
            case "COMPLETE" -> memberMission.missionStatus.eq(MissionStatus.SUCCESS);
            case "RETRY" -> memberMission.missionStatus.eq(MissionStatus.FAIL)
                    .or(memberMission.missionStatus.eq(MissionStatus.NONE).and(memberMission.attemptCount.gt(0)));
            default -> throw new MissionException(MissionErrorCode.INVALID_MISSION_STATUS_PARAM);
        };
    }
}