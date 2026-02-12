package com.example.moamoa_backend.domain.wallet.service.query;

import com.example.moamoa_backend.domain.item.entity.QItem;
import com.example.moamoa_backend.domain.mission.entity.QMission;
import com.example.moamoa_backend.domain.wallet.dto.WalletHistoryListRequestDto;
import com.example.moamoa_backend.domain.wallet.dto.WalletHistoryListResponseDto;
import com.example.moamoa_backend.domain.wallet.entity.QWallet;
import com.example.moamoa_backend.domain.wallet.entity.QWalletHistory;
import com.example.moamoa_backend.domain.wallet.entity.Wallet;
import com.example.moamoa_backend.domain.wallet.enums.TransactionType;
import com.example.moamoa_backend.domain.wallet.exception.WalletException;
import com.example.moamoa_backend.domain.wallet.exception.code.WalletErrorCode;
import com.example.moamoa_backend.domain.wallet.repository.WalletRepository;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/**
 * 지갑 내역 목록 조회(탭/정렬/기간/적립 소스 필터 + 페이징)를 제공하는 Query 서비스.
 *
 * 정책
 * - page는 1부터 시작
 * - size는 1~50 범위로 방어
 * - hasNext 판정을 위해 total count 기반 totalPages 계산
 */
public class WalletHistoryListQueryServiceImpl implements WalletHistoryListQueryService {
	private final WalletRepository walletRepository;
	private final JPAQueryFactory queryFactory;

	@Override
	/**
	 * 지갑 내역을 조건에 맞게 페이징 조회한다.
	 *
	 * @param memberId 회원 ID
	 * @param tab 탭(ALL/EARN/USE)
	 * @param sort 정렬(최신/오래된)
	 * @param period 기간(전체/3개월/6개월)
	 * @param earnSource 적립 소스(미션/출석/전체) - 적립 탭에서만 적용
	 * @param page 페이지(1부터)
	 * @param size 페이지 크기(최대 50)
	 * @return 지갑 내역 목록 응답 DTO
	 * @throws WalletException 지갑이 존재하지 않는 경우
	 */
	public WalletHistoryListResponseDto.Response getHistories(
		Long memberId,
		WalletHistoryListRequestDto.Tab tab,
		WalletHistoryListRequestDto.Sort sort,
		WalletHistoryListRequestDto.Period period,
		WalletHistoryListRequestDto.EarnSource earnSource,
		int page,
		int size
	) {
		int safePage = Math.max(page, 1);
		int safeSize = Math.min(Math.max(size, 1), 50);
		long offset = (long)(safePage - 1) * safeSize;

		Wallet wallet = walletRepository.findByMemberId(memberId)
			.orElseThrow(() -> new WalletException(WalletErrorCode.WALLET_NOT_FOUND));

		QWalletHistory wh = QWalletHistory.walletHistory;
		QWallet w = QWallet.wallet;
		QMission m = QMission.mission;
		QItem i = QItem.item;

		LocalDateTime startAt = resolveStartAt(period);

		BooleanExpression where = w.member.id.eq(memberId)
			.and(applyPeriod(wh, startAt))
			.and(applyTab(wh, tab))
			.and(applyEarnSource(wh, tab, earnSource));

		OrderSpecifier<?>[] order = (sort == WalletHistoryListRequestDto.Sort.OLDEST)
			? new OrderSpecifier[] {wh.createdAt.asc(), wh.id.asc()}
			: new OrderSpecifier[] {wh.createdAt.desc(), wh.id.desc()};

		// 1) total count 쿼리
		Long totalElementsObj = queryFactory
			.select(wh.count())
			.from(wh)
			.join(wh.wallet, w)
			.leftJoin(wh.mission, m)
			.leftJoin(wh.item, i)
			.where(where)
			.fetchOne();

		long totalElements = (totalElementsObj == null) ? 0L : totalElementsObj;
		int totalPages = (totalElements == 0) ? 0 : (int)Math.ceil((double)totalElements / safeSize);
		boolean hasNext = safePage < totalPages;

		// 2) 실제 page 데이터 조회 (offset/limit)
		List<WalletHistoryListResponseDto.Item> rows = queryFactory
			.select(
				Projections.constructor(
					WalletHistoryListResponseDto.Item.class,
					wh.id,
					wh.type,
					wh.amount,
					wh.createdAt,
					i.name.coalesce(m.title).coalesce(wh.type.stringValue()),
					new CaseBuilder()
						.when(wh.amount.goe(0)).then("도토리 적립")
						.otherwise("도토리 사용"),
					i.type
				)
			)
			.from(wh)
			.join(wh.wallet, w)
			.leftJoin(wh.mission, m)
			.leftJoin(wh.item, i)
			.where(where)
			.orderBy(order)
			.offset(offset)
			.limit(safeSize)
			.fetch();

		// title 보정
		List<WalletHistoryListResponseDto.Item> normalized = rows.stream()
			.map(it -> new WalletHistoryListResponseDto.Item(
				it.walletHistoryId(),
				it.type(),
				it.amount(),
				it.createdAt(),
				normalizeTitle(it),
				it.categoryLabel(),
				it.itemType()
			))
			.toList();

		return new WalletHistoryListResponseDto.Response(
			wallet.getPoint(),
			safePage,
			safeSize,
			totalElements,
			totalPages,
			hasNext,
			normalized
		);
	}

	/**
	 * type에 따라 title이 비어있을 때의 기본 타이틀을 보정한다.
	 *
	 * @param it 지갑 내역 아이템
	 * @return 보정된 title
	 */
	private String normalizeTitle(WalletHistoryListResponseDto.Item it) {
		if (it.type() == TransactionType.PURCHASE) {
			return (it.title() != null && !it.title().isBlank()) ? it.title() : it.type().getDescription();
		}
		if (it.type() == TransactionType.MISSION || it.type() == TransactionType.MISSION_COMPLETE) {
			return (it.title() != null && !it.title().isBlank()) ? it.title() : it.type().getDescription();
		}
		return it.type().getDescription();
	}

	/**
	 * 탭 조건(ALL/EARN/USE)을 where 조건으로 변환한다.
	 *
	 * @param wh QWalletHistory
	 * @param tab 탭
	 * @return BooleanExpression (ALL이면 null)
	 */
	private BooleanExpression applyTab(QWalletHistory wh, WalletHistoryListRequestDto.Tab tab) {
		if (tab == null || tab == WalletHistoryListRequestDto.Tab.ALL)
			return null;
		if (tab == WalletHistoryListRequestDto.Tab.EARN)
			return wh.amount.gt(0);
		if (tab == WalletHistoryListRequestDto.Tab.USE)
			return wh.amount.lt(0);
		return null;
	}

	/**
	 * 적립 탭에서만 적립 소스(미션/출석)를 필터링한다.
	 *
	 * @param wh QWalletHistory
	 * @param tab 탭
	 * @param earnSource 적립 소스
	 * @return BooleanExpression (미적용이면 null)
	 */
	private BooleanExpression applyEarnSource(QWalletHistory wh,
		WalletHistoryListRequestDto.Tab tab,
		WalletHistoryListRequestDto.EarnSource earnSource) {
		if (tab != WalletHistoryListRequestDto.Tab.EARN)
			return null;
		if (earnSource == null || earnSource == WalletHistoryListRequestDto.EarnSource.ALL)
			return null;

		return switch (earnSource) {
			case MISSION -> wh.type.in(TransactionType.MISSION, TransactionType.MISSION_COMPLETE);
			case ATTENDANCE -> wh.type.in(TransactionType.ATTENDANCE, TransactionType.ATTENDANCE_STREAK_BONUS);
			default -> null;
		};
	}

	/**
	 * 기간 조건(시작 시각)을 where 조건으로 변환한다.
	 *
	 * @param wh QWalletHistory
	 * @param startAt 시작 시각 (없으면 null)
	 * @return BooleanExpression (미적용이면 null)
	 */
	private BooleanExpression applyPeriod(QWalletHistory wh, LocalDateTime startAt) {
		if (startAt == null)
			return null;
		return wh.createdAt.goe(startAt);
	}

	/**
	 * 기간(3개월/6개월)에 따른 시작 시각을 계산한다.
	 *
	 * @param period 기간 enum
	 * @return 시작 시각 (ALL이면 null)
	 */
	private LocalDateTime resolveStartAt(WalletHistoryListRequestDto.Period period) {
		if (period == null || period == WalletHistoryListRequestDto.Period.ALL)
			return null;

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime raw = switch (period) {
			case THREE_MONTHS -> now.minusMonths(3);
			case SIX_MONTHS -> now.minusMonths(6);
			default -> null;
		};
		if (raw == null)
			return null;

		return raw.toLocalDate().atTime(LocalTime.MIN);
	}
}
