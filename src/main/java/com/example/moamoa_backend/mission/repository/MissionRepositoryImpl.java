package com.example.moamoa_backend.mission.repository;

import com.example.moamoa_backend.mission.entity.Mission;
import com.example.moamoa_backend.mission.enums.MissionStatus;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import java.util.List;
import static com.example.moamoa_backend.member.entity.mapping.QMemberMission.memberMission;
import static com.example.moamoa_backend.mission.entity.QMission.mission;

@RequiredArgsConstructor
public class MissionRepositoryImpl implements MissionRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Mission> findTodayRecommendMission(Long memberId, List<Long> interestIds, Integer time) {

        // 1. 찜이면 1순위
        OrderSpecifier<Integer> rankByScrap = new CaseBuilder()
                .when(memberMission.missionStatus.eq(MissionStatus.SCRAP)).then(1)
                .otherwise(2).asc();

        // 2. 찜 내부 시간순
        OrderSpecifier<Integer> rankByTimeForScrap = new CaseBuilder()
                .when(memberMission.missionStatus.eq(MissionStatus.SCRAP)).then(mission.durationMinutes)
                .otherwise((Integer) null).asc();

        OrderSpecifier<Double> rankByRandom = Expressions.numberTemplate(Double.class, "function('rand')").asc();

        return queryFactory
                .selectFrom(mission)
                .leftJoin(memberMission)
                .on(memberMission.mission.id.eq(mission.id)
                        .and(memberMission.member.id.eq(memberId)))
                .where(
                        mission.missionSubInterests.any().subInterest.interest.id.in(interestIds),

                        durationLoe(time),

                        memberMission.missionStatus.isNull()
                                .or(memberMission.missionStatus.eq(MissionStatus.SCRAP))
                                .or(memberMission.missionStatus.eq(MissionStatus.NONE)
                                        .and(memberMission.attemptCount.eq(0)))
                )
                .orderBy(
                        rankByScrap,
                        rankByTimeForScrap,
                        rankByRandom
                )
                .limit(5)
                .fetch();
    }

    private BooleanExpression durationLoe(Integer time) {
        return time != null ? mission.durationMinutes.loe(time) : null;
    }
}