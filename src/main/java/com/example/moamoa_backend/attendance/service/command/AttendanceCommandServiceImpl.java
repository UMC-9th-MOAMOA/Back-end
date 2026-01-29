package com.example.moamoa_backend.attendance.service.command;

import com.example.moamoa_backend.attendance.converter.AttendanceConverter;
import com.example.moamoa_backend.attendance.dto.AttendanceResponseDto;
import com.example.moamoa_backend.attendance.entity.Attendance;
import com.example.moamoa_backend.attendance.entity.AttendanceStreak;
import com.example.moamoa_backend.attendance.exception.AttendanceException;
import com.example.moamoa_backend.attendance.exception.code.AttendanceErrorCode;
import com.example.moamoa_backend.attendance.repository.AttendanceRepository;
import com.example.moamoa_backend.attendance.repository.AttendanceStreakRepository;
import com.example.moamoa_backend.global.util.ObjectRedisUtil;
import com.example.moamoa_backend.member.entity.Member;
import com.example.moamoa_backend.member.repository.MemberRepository;
import com.example.moamoa_backend.wallet.entity.Wallet;
import com.example.moamoa_backend.wallet.entity.WalletHistory;
import com.example.moamoa_backend.wallet.enums.TransactionType;
import com.example.moamoa_backend.wallet.repository.WalletHistoryRepository;
import com.example.moamoa_backend.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceCommandServiceImpl implements AttendanceCommandService{

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int ATTENDANCE_REWARD = 1;
    private static final int STREAK_7_BONUS = 10;

    private final ObjectRedisUtil objectRedisUtil;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceStreakRepository streakRepository;
    private final MemberRepository memberRepository;

    private final WalletRepository walletRepository;
    private final WalletHistoryRepository walletHistoryRepository;

    @Override
    public AttendanceResponseDto.CheckInResult checkIn(Long memberId) {
        LocalDate today = LocalDate.now(KST);

        String redisKey = buildTodayKey(memberId, today);
        long ttlSeconds = secondsUntilEndOfDay(KST);

        // 1) Redis 중복 방지
        boolean first = objectRedisUtil.setIfAbsentExpire(redisKey, 1, ttlSeconds);
        if (!first) throw new AttendanceException(AttendanceErrorCode.ALREADY_ATTENDED);

        try {
            // 2) Member 확인
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new AttendanceException(AttendanceErrorCode.MEMBER_NOT_FOUND));

            // 3) Attendance 저장 (DB 최종 중복 방어)
            try {
                attendanceRepository.save(Attendance.create(member, today));
            } catch (DataIntegrityViolationException e) {
                // 중복 키 위반인지 확인 (다른 무결성 오류와 구분)
                if (attendanceRepository.existsByMember_IdAndAttendanceDate(memberId, today)) {
                    throw new AttendanceException(AttendanceErrorCode.ALREADY_ATTENDED);
                }
                throw e; // 다른 무결성 오류는 그대로 전파
            }

            // 4) streak 갱신 (락)
            AttendanceStreak streak = streakRepository.findByMemberIdForUpdate(memberId)
                    .orElseGet(() -> streakRepository.save(AttendanceStreak.create(member)));

            int newStreak = calculateNewStreak(streak, today);
            boolean completedToday = (newStreak == 7);

            streak.applyToday(today, completedToday ? 7 : newStreak, completedToday);

            // 5) Wallet + WalletHistory (락)
            Wallet wallet = walletRepository.findByMemberIdForUpdate(memberId)
                    .orElseGet(() -> walletRepository.save(Wallet.create(member)));

            // ✅ 출석 보상 +1 (type=ATTENDANCE)
            wallet.addPoint(ATTENDANCE_REWARD);
            walletHistoryRepository.save(
                    WalletHistory.create(
                            wallet,
                            null,
                            null,
                            "출석 보상으로 도토리를 1개 받았다",
                            ATTENDANCE_REWARD,
                            wallet.getPoint(),
                            TransactionType.ATTENDANCE
                    )
            );

            // ✅ 연속 7일 달성 보너스 +10 (type=ATTENDANCE_STREAK_BONUS)
            if (completedToday) {
                wallet.addPoint(STREAK_7_BONUS);
                walletHistoryRepository.save(
                        WalletHistory.create(
                                wallet,
                                null,
                                null,
                                "연속 7일 출석 달성 보너스로 도토리를 10개 받았다",
                                STREAK_7_BONUS,
                                wallet.getPoint(),
                                TransactionType.ATTENDANCE_STREAK_BONUS
                        )
                );
            }

            return AttendanceConverter.toCheckInResult(today, true, streak.getCurrentStreak(), completedToday);

        } catch (RuntimeException ex) {
            // DB 실패시 redis 롤백
            objectRedisUtil.delete(redisKey);
            throw ex;
        }
    }

    private int calculateNewStreak(AttendanceStreak streak, LocalDate today) {
        LocalDate yesterday = today.minusDays(1);

        if (streak.getLastCompletedDate() != null && streak.getLastCompletedDate().equals(yesterday)) {
            return 1;
        }

        if (streak.getLastAttendedDate() != null && streak.getLastAttendedDate().equals(yesterday)) {
            return streak.getCurrentStreak() + 1;
        }

        return 1;
    }

    private String buildTodayKey(Long memberId, LocalDate date) {
        return "att:member:" + memberId + ":date:" + date;
    }

    private long secondsUntilEndOfDay(ZoneId zoneId) {
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime nextDayStart = now.toLocalDate().plusDays(1).atStartOfDay(zoneId);
        return Duration.between(now, nextDayStart).getSeconds();
    }
}