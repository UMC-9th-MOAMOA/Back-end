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

    private final ObjectRedisUtil objectRedisUtil;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceStreakRepository streakRepository;
    private final MemberRepository memberRepository;

    @Override
    public AttendanceResponseDto.CheckInResult checkIn(Long memberId) {
        LocalDate today = LocalDate.now(KST);

        String redisKey = buildTodayKey(memberId, today);
        long ttlSeconds = secondsUntilEndOfDay(KST);

        // 1) Redis: 오늘 중복 출석 방지 (SETNX)
        boolean first = objectRedisUtil.setIfAbsentExpire(redisKey, 1, ttlSeconds);
        if (!first) {
            // 여기서 너의 예외로 변환
            throw new AttendanceException(AttendanceErrorCode.ALREADY_ATTENDED); // TODO: AttendanceException으로 교체
        }

        try {
            // 2) DB: Member 확인
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() ->
                            new AttendanceException(AttendanceErrorCode.MEMBER_NOT_FOUND)
                    ); // TODO: AttendanceException

            // 3) DB: Attendance 로그 저장 (유니크로 최종 방어)
            try {
                attendanceRepository.save(Attendance.create(member, today));
            } catch (DataIntegrityViolationException e) {
                throw new AttendanceException(AttendanceErrorCode.ALREADY_ATTENDED); // TODO: AttendanceException
            }

            // 4) DB: streak 상태 갱신 (락)
            AttendanceStreak streak = streakRepository.findByMemberIdForUpdate(memberId)
                    .orElseGet(() -> streakRepository.save(AttendanceStreak.create(member)));


            int newStreak = calculateNewStreak(streak, today);
            boolean completedToday = (newStreak == 7);

            streak.applyToday(today, completedToday ? 7 : newStreak, completedToday);

            return AttendanceConverter.toCheckInResult(today, true, streak.getCurrentStreak(), completedToday);

        } catch (RuntimeException ex) {
            // 🔥 중요: DB쪽에서 실패하면 Redis 키를 지워서 "재시도 가능"하게
            objectRedisUtil.delete(redisKey);
            throw ex;
        }
    }

    /**
     * 요구사항:
     * - 전날 미출석이면 1로 리셋
     * - 전날 출석이면 +1
     * - 7일 달성 시 성공 처리, 다음날 streak 1부터 재시작
     */
    private int calculateNewStreak(AttendanceStreak streak, LocalDate today) {
        LocalDate yesterday = today.minusDays(1);

        // 어제가 "7일 달성일"이면 오늘 1부터 재시작
        if (streak.getLastCompletedDate() != null && streak.getLastCompletedDate().equals(yesterday)) {
            return 1;
        }

        // 어제 출석했으면 +1, 아니면 1
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
