package com.example.moamoa_backend.domain.wallet.service.query;

import com.example.moamoa_backend.domain.mission.entity.QMission;
import com.example.moamoa_backend.domain.wallet.dto.WalletHistoryDayResponseDto;
import com.example.moamoa_backend.domain.wallet.dto.WalletPointResponseDto;
import com.example.moamoa_backend.domain.wallet.entity.QWallet;
import com.example.moamoa_backend.domain.wallet.entity.QWalletHistory;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/**
 * 지갑 내역(일자별) 및 보유 포인트를 조회하는 Query 서비스.
 */
public class WalletHistoryQueryServiceImpl implements WalletHistoryQueryService {
	private final JPAQueryFactory queryFactory;
	private final QWallet wallet = QWallet.wallet;

	@Override
	/**
	 * 특정 날짜의 지갑 적립 내역을 조회한다.
	 *
	 * 조회 범위
	 * - [date 00:00, date+1 00:00)
	 *
	 * 조회 조건
	 * - amount > 0 (적립 내역만)
	 * - Mission은 left join으로 함께 조회
	 *
	 * @param memberId 회원 ID
	 * @param date 조회 날짜
	 * @return 일자별 지갑 내역 응답 DTO
	 */
	public WalletHistoryDayResponseDto.Response getDayHistory(Long memberId, LocalDate date) {
		LocalDateTime start = date.atStartOfDay();
		LocalDateTime end = date.plusDays(1).atStartOfDay();

		QWalletHistory wh = QWalletHistory.walletHistory;
		QWallet w = QWallet.wallet;
		QMission m = QMission.mission;

		List<WalletHistoryDayResponseDto.Item> items = queryFactory
			.select(
				com.querydsl.core.types.Projections.constructor(
					WalletHistoryDayResponseDto.Item.class,
					wh.type,
					wh.amount,
					wh.createdAt,
					m.id,
					m.title,
					m.durationMinutes
				)
			)
			.from(wh)
			.join(wh.wallet, w)
			.leftJoin(wh.mission, m)
			.where(
				w.member.id.eq(memberId),
				wh.createdAt.goe(start),
				wh.createdAt.lt(end),
				wh.amount.gt(0)
			)
			.orderBy(wh.createdAt.asc())
			.fetch();

		int totalMinutes = items.stream()
			.map(WalletHistoryDayResponseDto.Item::missionDurationMinutes)
			.filter(v -> v != null)
			.mapToInt(Integer::intValue)
			.sum();

		int totalAcorns = items.stream()
			.mapToInt(WalletHistoryDayResponseDto.Item::amount)
			.sum();

		return new WalletHistoryDayResponseDto.Response(date, items, totalMinutes, totalAcorns);
	}

	@Override
	/**
	 * 내 지갑 포인트를 조회한다.
	 *
	 * @param memberId 회원 ID
	 * @return 보유 포인트 응답 DTO
	 */
	public WalletPointResponseDto.Response getMyPoint(Long memberId) {
		Integer point = queryFactory
			.select(wallet.point)
			.from(wallet)
			.where(wallet.member.id.eq(memberId))
			.fetchOne();

		return new WalletPointResponseDto.Response(point);
	}
}
