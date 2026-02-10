package com.example.moamoa_backend.domain.item.service.query;

import com.example.moamoa_backend.attendance.entity.QAttendanceStreak;
import com.example.moamoa_backend.domain.item.dto.HomePocketResponseDto;
import com.example.moamoa_backend.domain.member.entity.Member;
import com.example.moamoa_backend.member.entity.QMember;
import com.example.moamoa_backend.member.entity.mapping.QMemberMission;
import com.example.moamoa_backend.domain.member.exception.MemberException;
import com.example.moamoa_backend.domain.member.exception.code.MemberErrorCode;
import com.example.moamoa_backend.mission.entity.QMission;
import com.example.moamoa_backend.domain.mission.enums.MissionStatus;
import com.example.moamoa_backend.wallet.entity.QWallet;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberHomeQueryServiceImpl implements MemberHomeQueryService{

    private static final ZoneId KST = ZoneId.of("Asia/Seoul"); // 추가

    private final JPAQueryFactory queryFactory;

    private final QMember member = QMember.member;
    private final QWallet wallet = QWallet.wallet;
    private final QAttendanceStreak streak = QAttendanceStreak.attendanceStreak;

    private final QMemberMission mm = QMemberMission.memberMission;

    private final QMission mission = QMission.mission;

    @Override
    public HomePocketResponseDto.Response getHomePocket(Long memberId) {

        // 1) member 조회 (목표 필드가 member에 있음)
        Member m = queryFactory
                .selectFrom(member)
                .where(member.id.eq(memberId))
                .fetchOne();

        if (m == null) {
            throw new MemberException(MemberErrorCode.MEMBER_NOT_FOUND);
        }

        LocalDate today = LocalDate.now(KST);
        LocalDate thisWeekMon = today.with(DayOfWeek.MONDAY);
        LocalDate nextWeekMon = thisWeekMon.plusWeeks(1);

        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();

        LocalDateTime thisWeekStart = thisWeekMon.atStartOfDay();
        LocalDateTime nextWeekStart = nextWeekMon.atStartOfDay();

        // 2) 오늘/이번주 미션 수행시간 합 (SUCCESS 기준)
        Long todayMinutes = queryFactory
                .select(
                        Expressions.numberTemplate(
                                Long.class,
                                "coalesce(sum({0}), 0)",
                                mission.durationMinutes
                        )
                )
                .from(mm)
                .join(mm.mission, mission)
                .where(
                        mm.member.id.eq(memberId),
                        mm.createdAt.goe(todayStart),
                        mm.createdAt.lt(tomorrowStart)
                )
                .fetchOne();

        Long thisWeekMinutes = queryFactory
                .select(
                        Expressions.numberTemplate(
                                Long.class,
                                "coalesce(sum({0}), 0)",
                                mission.durationMinutes
                        )
                )
                .from(mm)
                .join(mm.mission, mission)
                .where(
                        mm.member.id.eq(memberId),
                        mm.createdAt.goe(thisWeekStart),
                        mm.createdAt.lt(nextWeekStart)
                )
                .fetchOne();

        long todayMissionMinutes = todayMinutes == null ? 0L : todayMinutes;
        long thisWeekMissionMinutes = thisWeekMinutes == null ? 0L : thisWeekMinutes;

        // 3) wallet point
        Integer point = queryFactory
                .select(wallet.point)
                .from(wallet)
                .where(wallet.member.id.eq(memberId))
                .fetchOne();
        int walletPoint = point;

        // 4) attendance streak
        Integer currentStreak = queryFactory
                .select(streak.currentStreak)
                .from(streak)
                .where(streak.member.id.eq(memberId))
                .fetchOne();
        int streakCount = currentStreak == null ? 0 : currentStreak;

        // 5) 목표 진행
        HomePocketResponseDto.GoalProgress goalProgress = null;

        if (m.getDailyGoal() != null) {

            DateTemplate<Date> rewardDate =
                    Expressions.dateTemplate(Date.class, "date({0})", mm.rewardAt);

            NumberExpression<Long> countExpr = mm.id.count();

            List<Tuple> tuples = queryFactory
                    .select(rewardDate, countExpr)
                    .from(mm)
                    .where(
                            mm.member.id.eq(memberId),
                            mm.missionStatus.eq(MissionStatus.SUCCESS),
                            mm.attemptCount.eq(1),
                            mm.rewardAt.goe(thisWeekStart),
                            mm.rewardAt.lt(nextWeekStart)
                    )
                    .groupBy(rewardDate)
                    .fetch();

            Map<LocalDate, Long> thisWeekMap = new HashMap<>();
            for (Tuple t : tuples) {
                Date sqlDate = t.get(rewardDate);          // java.sql.Date로 받기
                LocalDate localDate = sqlDate.toLocalDate(); // LocalDate로 변환
                thisWeekMap.put(localDate, t.get(countExpr));
            }

            List<Long> thisWeekDaily = new ArrayList<>(7);
            long thisWeekTotal = 0;

            for (int i = 0; i < 7; i++) {
                LocalDate d = thisWeekMon.plusDays(i);
                long cnt = thisWeekMap.getOrDefault(d, 0L);
                thisWeekDaily.add(cnt);
                thisWeekTotal += cnt;
            }

            // ✅ 저번주 총합
            LocalDate lastWeekMon = thisWeekMon.minusWeeks(1);
            LocalDateTime lastWeekStart = lastWeekMon.atStartOfDay();
            LocalDateTime thisWeekStart2 = thisWeekMon.atStartOfDay();

            Long lastWeekTotal = queryFactory
                    .select(mm.id.count())
                    .from(mm)
                    .where(
                            mm.member.id.eq(memberId),
                            mm.missionStatus.eq(MissionStatus.SUCCESS),
                            mm.attemptCount.eq(1),
                            mm.rewardAt.goe(lastWeekStart),
                            mm.rewardAt.lt(thisWeekStart2)
                    )
                    .fetchOne();

            goalProgress = new HomePocketResponseDto.GoalProgress(
                    m.getDailyGoal(),
                    lastWeekTotal == null ? 0L : lastWeekTotal,
                    thisWeekTotal,
                    thisWeekDaily
            );
        }

        return new HomePocketResponseDto.Response(
                m.getName(),
                todayMissionMinutes,
                thisWeekMissionMinutes,
                walletPoint,
                streakCount,
                goalProgress
        );
    }
}
