package com.example.moamoa_backend.mission.repository;

import com.example.moamoa_backend.mission.entity.Mission;
import com.example.moamoa_backend.mission.enums.MissionStatus;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;
import static com.example.moamoa_backend.member.entity.mapping.QMemberMission.memberMission;
import static com.example.moamoa_backend.mission.entity.QMission.mission;

@RequiredArgsConstructor
public class MissionRepositoryImpl implements MissionRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Mission> findTodayRecommendMission(Long memberId, List<Long> interestIds, Integer time) {


        OrderSpecifier<Double> rankByRandom = Expressions.numberTemplate(Double.class, "function('rand')").asc();

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
                        rankByRandom
                )
                .limit(5)
                .fetch();
    }

//    public Slice<Mission> searchMissions(Long memberId, String searchText, List<String> keywords, Pageable pageable){
//
//        List<Mission> results = queryFactory
//                .selectFrom(mission)
//                .leftJoin(memberMission)
//                .on(memberMission.mission.id.eq(mission.id)
//                        .and(memberMission.member.id.eq(memberId))
//                )
//                .where(
//                        contains
//                )
//                .orderBy(
//
//                )
//    }

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

}