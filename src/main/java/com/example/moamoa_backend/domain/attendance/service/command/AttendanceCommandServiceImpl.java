package com.example.moamoa_backend.domain.attendance.service.command;

import com.example.moamoa_backend.domain.attendance.converter.AttendanceConverter;
import com.example.moamoa_backend.domain.attendance.dto.AttendanceResponseDto;
import com.example.moamoa_backend.domain.attendance.entity.Attendance;
import com.example.moamoa_backend.domain.attendance.entity.AttendanceStreak;
import com.example.moamoa_backend.domain.attendance.exception.AttendanceException;
import com.example.moamoa_backend.domain.attendance.exception.code.AttendanceErrorCode;
import com.example.moamoa_backend.domain.attendance.repository.AttendanceRepository;
import com.example.moamoa_backend.domain.attendance.repository.AttendanceStreakRepository;
import com.example.moamoa_backend.global.util.ObjectRedisUtil;
import com.example.moamoa_backend.domain.member.entity.Member;
import com.example.moamoa_backend.domain.member.repository.MemberRepository;
import com.example.moamoa_backend.domain.wallet.entity.Wallet;
import com.example.moamoa_backend.domain.wallet.entity.WalletHistory;
import com.example.moamoa_backend.domain.wallet.enums.TransactionType;
import com.example.moamoa_backend.domain.wallet.repository.WalletHistoryRepository;
import com.example.moamoa_backend.domain.wallet.repository.WalletRepository;

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
/**
 * 출석 체크인(출석 기록 + 연속 출석 + 보상 지급)을 처리하는 Command 서비스.
 *
 * 주요 정책
 * - Redis(일자 TTL)로 1차 중복 방지
 * - DB Unique 제약으로 2차 중복 방지
 * - streak, wallet은 SELECT ... FOR UPDATE로 동시성 제어
 * - DB 처리 실패 시 Redis 키를 롤백 삭제
 */
public class AttendanceCommandServiceImpl implements AttendanceCommandService {

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
	/**
	 * 출석 체크인을 수행한다.
	 *
	 * 처리 흐름
	 * 1) Redis 키로 당일 중복 체크인 차단
	 * 2) Member 존재 확인
	 * 3) Attendance 저장 (DB 중복/무결성 방어)
	 * 4) AttendanceStreak 갱신 (락)
	 * 5) Wallet 포인트 적립 + WalletHistory 기록 (락)
	 *
	 * @param memberId 출석 체크인을 수행할 회원 ID
	 * @return 출석 체크인 결과 DTO
	 * @throws AttendanceException 이미 출석했거나 회원이 없을 때
	 */
	public AttendanceResponseDto.CheckInResult checkIn(Long memberId) {
		LocalDate today = LocalDate.now(KST);

		String redisKey = buildTodayKey(memberId, today);
		long ttlSeconds = secondsUntilEndOfDay(KST);

		// 1) Redis 중복 방지
		boolean first = objectRedisUtil.setIfAbsentExpire(redisKey, 1, ttlSeconds);
		if (!first)
			throw new AttendanceException(AttendanceErrorCode.ALREADY_ATTENDED);

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

			// 출석 보상 +1 (type=ATTENDANCE)
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

			// 연속 7일 달성 보너스 +10 (type=ATTENDANCE_STREAK_BONUS)
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

	/**
	 * 오늘 체크인 시 새로운 연속 출석 일수를 계산한다.
	 *
	 * 규칙
	 * - 어제(lastAttendedDate)가 어제면 연속 +1
	 * - 그 외는 1로 리셋
	 *
	 * @param streak 회원의 출석 streak 엔티티
	 * @param today 오늘 날짜
	 * @return 계산된 새로운 streak 값
	 */
	private int calculateNewStreak(AttendanceStreak streak, LocalDate today) {
		LocalDate yesterday = LocalDate.now(KST).minusDays(1);

		if (streak.getLastCompletedDate() != null && streak.getLastCompletedDate().equals(yesterday)) {
			return 1;
		}

		if (streak.getLastAttendedDate() != null && streak.getLastAttendedDate().equals(yesterday)) {
			return streak.getCurrentStreak() + 1;
		}

		return 1;
	}

	/**
	 * Redis 중복 방지를 위한 당일 키를 생성한다.
	 *
	 * @param memberId 회원 ID
	 * @param date 날짜
	 * @return redis key 문자열
	 */
	private String buildTodayKey(Long memberId, LocalDate date) {
		return "att:member:" + memberId + ":date:" + date;
	}

	/**
	 * 현재 시각부터 해당 타임존 기준 "내일 00:00"까지 남은 초(TTL)를 계산한다.
	 *
	 * @param zoneId 기준 타임존
	 * @return 남은 초(Seconds)
	 */
	private long secondsUntilEndOfDay(ZoneId zoneId) {
		ZonedDateTime now = ZonedDateTime.now(zoneId);
		ZonedDateTime nextDayStart = now.toLocalDate().plusDays(1).atStartOfDay(zoneId);
		return Duration.between(now, nextDayStart).getSeconds();
	}
}
