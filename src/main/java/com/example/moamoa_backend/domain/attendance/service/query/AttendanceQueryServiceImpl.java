package com.example.moamoa_backend.domain.attendance.service.query;

import com.example.moamoa_backend.domain.attendance.dto.AttendanceMonthResponseDto;
import com.example.moamoa_backend.domain.attendance.entity.QAttendance;
import com.example.moamoa_backend.domain.attendance.repository.AttendanceStreakRepository;
import com.example.moamoa_backend.domain.wallet.entity.QWallet;
import com.example.moamoa_backend.domain.wallet.entity.QWalletHistory;
import com.example.moamoa_backend.domain.wallet.enums.TransactionType;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/**
 * 출석 조회(연속 출석, 월별 출석/미션 보상 일자)를 제공하는 Query 서비스.
 */
public class AttendanceQueryServiceImpl implements AttendanceQueryService {

	private final AttendanceStreakRepository attendanceStreakRepository;
	private final JPAQueryFactory queryFactory;

	@Override
	/**
	 * 현재 연속 출석 일수를 조회한다.
	 *
	 * @param memberId 회원 ID
	 * @return 현재 연속 출석 일수 (없으면 0)
	 */
	public int getCurrentStreak(Long memberId) {
		return attendanceStreakRepository.findByMember_Id(memberId)
			.map(s -> Math.max(0, s.getCurrentStreak()))
			.orElse(0);
	}

	@Override
	/**
	 * 월별 출석 현황을 조회한다.
	 *
	 * 반환 데이터
	 * - attendedDays: Attendance.attendanceDate 기준 출석한 날짜(일자 목록)
	 * - missionRewardDays: WalletHistory.createdAt + (미션 관련 타입) 기준 보상 발생 날짜(일자 목록)
	 *
	 * @param memberId 회원 ID
	 * @param year 조회 연도
	 * @param month 조회 월(1~12)
	 * @return 월별 출석 현황 DTO
	 */
	public AttendanceMonthResponseDto.Response getMonthStatus(Long memberId, int year, int month) {

		YearMonth ym = YearMonth.of(year, month);
		LocalDate startDate = ym.atDay(1);
		LocalDate endDateExclusive = ym.plusMonths(1).atDay(1);

		// 1) 월별 출석 일자(Attendance.attendanceDate)
		QAttendance a = QAttendance.attendance;

		List<Integer> attendedDays = queryFactory
			.select(a.attendanceDate.dayOfMonth())
			.from(a)
			.where(
				a.member.id.eq(memberId),
				a.attendanceDate.goe(startDate),
				a.attendanceDate.lt(endDateExclusive)
			)
			.distinct()
			.orderBy(a.attendanceDate.dayOfMonth().asc())
			.fetch();

		// 2) 월별 미션 보상 일자(WalletHistory.createdAt + type == MISSION)
		QWalletHistory wh = QWalletHistory.walletHistory;
		QWallet w = QWallet.wallet;

		LocalDateTime startAt = startDate.atStartOfDay();
		LocalDateTime endAt = endDateExclusive.atStartOfDay();

		// MySQL date(createdAt)
		var createdDateExpr = Expressions.dateTemplate(Date.class, "date({0})", wh.createdAt);

		List<Date> missionRewardSqlDates = queryFactory
			.select(createdDateExpr)
			.from(wh)
			.join(wh.wallet, w)
			.where(
				w.member.id.eq(memberId),
				wh.type.in(TransactionType.MISSION, TransactionType.MISSION_COMPLETE),
				wh.createdAt.goe(startAt),
				wh.createdAt.lt(endAt)
			)
			.distinct()
			.orderBy(createdDateExpr.asc())
			.fetch();

		List<Integer> missionRewardDays = missionRewardSqlDates.stream()
			.map(Date::toLocalDate)
			.map(LocalDate::getDayOfMonth)
			.distinct()
			.toList();

		return new AttendanceMonthResponseDto.Response(
			year,
			month,
			attendedDays,
			missionRewardDays
		);
	}
}
