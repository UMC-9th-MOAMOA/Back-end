package com.example.moamoa_backend.attendance.service.query;

import com.example.moamoa_backend.attendance.dto.AttendanceMonthResponseDto;
import com.example.moamoa_backend.attendance.entity.QAttendance;
import com.example.moamoa_backend.attendance.repository.AttendanceStreakRepository;
import com.example.moamoa_backend.wallet.entity.QWallet;
import com.example.moamoa_backend.wallet.entity.QWalletHistory;
import com.example.moamoa_backend.wallet.enums.TransactionType;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceQueryServiceImpl implements AttendanceQueryService{

    private final AttendanceStreakRepository attendanceStreakRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public int getCurrentStreak(Long memberId) {
        return attendanceStreakRepository.findByMember_Id(memberId)
                .map(s -> Math.max(0, s.getCurrentStreak()))
                .orElse(0);
    }

    @Override
    public AttendanceMonthResponseDto.Response getMonthStatus(Long memberId, int year, int month) {

        YearMonth ym = YearMonth.of(year, month);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDateExclusive = ym.plusMonths(1).atDay(1);

        // ✅ 1) 월별 출석 일자(Attendance.attendanceDate)
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

        // ✅ 2) 월별 미션 보상 일자(WalletHistory.createdAt + type == MISSION)
        QWalletHistory wh = QWalletHistory.walletHistory;
        QWallet w = QWallet.wallet;

        LocalDateTime startAt = startDate.atStartOfDay();
        LocalDateTime endAt = endDateExclusive.atStartOfDay();

        // MySQL date(createdAt)
        var createdDateExpr = Expressions.dateTemplate(LocalDate.class, "date({0})", wh.createdAt);

        List<LocalDate> missionRewardDates = queryFactory
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

        List<Integer> missionRewardDays = missionRewardDates.stream()
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
