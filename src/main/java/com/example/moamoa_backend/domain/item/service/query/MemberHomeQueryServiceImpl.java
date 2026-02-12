package com.example.moamoa_backend.domain.item.service.query;

import com.example.moamoa_backend.domain.attendance.entity.QAttendanceStreak;
import com.example.moamoa_backend.domain.item.dto.HomePocketResponseDto;
import com.example.moamoa_backend.domain.member.entity.Member;
import com.example.moamoa_backend.domain.member.entity.QMember;
import com.example.moamoa_backend.domain.member.entity.mapping.QMemberMission;
import com.example.moamoa_backend.domain.member.exception.MemberException;
import com.example.moamoa_backend.domain.member.exception.code.MemberErrorCode;
import com.example.moamoa_backend.domain.mission.entity.QMission;
import com.example.moamoa_backend.domain.mission.enums.MissionStatus;
import com.example.moamoa_backend.domain.wallet.entity.QWallet;
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
/**
 * 홈 화면(Home Pocket)에 필요한 회원 요약 정보를 조회하는 Query 서비스.
 *
 * 조회 항목
 * - 오늘/이번주 미션 수행 시간 합
 * - 지갑 포인트
 * - 출석 연속 일수
 * - 목표 진행(주간 일자별 성공 카운트, 지난주 총합, 이번주 총합)
 */
public class MemberHomeQueryServiceImpl implements MemberHomeQueryService {

	private static final ZoneId KST = ZoneId.of("Asia/Seoul");

	private final JPAQueryFactory queryFactory;

	private final QMember member = QMember.member;
	private final QWallet wallet = QWallet.wallet;
	private final QAttendanceStreak streak = QAttendanceStreak.attendanceStreak;

	private final QMemberMission mm = QMemberMission.memberMission;

	private final QMission mission = QMission.mission;

	@Override
	/**
	 * 홈 포켓 데이터를 조회한다.
	 *
	 * @param memberId 회원 ID
	 * @return 홈 포켓 응답 DTO
	 * @throws MemberException 회원이 존재하지 않는 경우
	 */
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

		// 2) 오늘/이번주 미션 시청시간 합
		Long todaySeconds = queryFactory
			.select(
				Expressions.numberTemplate(
					Long.class,
					"coalesce(sum({0}), 0)",
						mission.videoLength
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

		Long thisWeekSeconds = queryFactory
			.select(
				Expressions.numberTemplate(
					Long.class,
					"coalesce(sum({0}), 0)",
						mission.videoLength
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

		long todayMissionMinutes =
				(todaySeconds == null ? 0L : todaySeconds) / 60;
		long thisWeekMissionMinutes =
				(thisWeekSeconds == null ? 0L : thisWeekSeconds) / 60;

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
				Date sqlDate = t.get(rewardDate);
				LocalDate localDate = sqlDate.toLocalDate();
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

			// 지난주 총합
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
