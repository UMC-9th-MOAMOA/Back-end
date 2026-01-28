package com.example.moamoa_backend.wallet.service.query;

import com.example.moamoa_backend.item.entity.QItem;
import com.example.moamoa_backend.mission.entity.QMission;
import com.example.moamoa_backend.wallet.dto.WalletHistoryListRequestDto;
import com.example.moamoa_backend.wallet.dto.WalletHistoryListResponseDto;
import com.example.moamoa_backend.wallet.entity.QWallet;
import com.example.moamoa_backend.wallet.entity.QWalletHistory;
import com.example.moamoa_backend.wallet.entity.Wallet;
import com.example.moamoa_backend.wallet.enums.TransactionType;
import com.example.moamoa_backend.wallet.exception.WalletException;
import com.example.moamoa_backend.wallet.exception.code.WalletErrorCode;
import com.example.moamoa_backend.wallet.repository.WalletRepository;
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
public class WalletHistoryListQueryServiceImpl implements WalletHistoryListQueryService{
    private final WalletRepository walletRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public WalletHistoryListResponseDto.Response getHistories(
            Long memberId,
            WalletHistoryListRequestDto.Tab tab,
            WalletHistoryListRequestDto.Sort sort,
            WalletHistoryListRequestDto.Period period,
            WalletHistoryListRequestDto.EarnSource earnSource
    ) {
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

        List<WalletHistoryListResponseDto.Item> rows = queryFactory
                .select(
                        Projections.constructor(
                                WalletHistoryListResponseDto.Item.class,
                                wh.id,
                                wh.type,
                                wh.amount,
                                wh.createdAt,

                                // 후보 title: PURCHASE=item.name, 미션류=mission.title, 그 외는 type명(아래 normalize에서 description으로)
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
                .orderBy(sort == WalletHistoryListRequestDto.Sort.OLDEST ? wh.createdAt.asc() : wh.createdAt.desc())
                .fetch();

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

        return new WalletHistoryListResponseDto.Response(wallet.getPoint(), normalized);
    }

    private String normalizeTitle(WalletHistoryListResponseDto.Item it) {
        // 구매: 아이템명 우선
        if (it.type() == TransactionType.PURCHASE) {
            return (it.title() != null && !it.title().isBlank()) ? it.title() : it.type().getDescription();
        }

        // 미션 성공/완료: 미션명 우선
        if (it.type() == TransactionType.MISSION || it.type() == TransactionType.MISSION_COMPLETE) {
            return (it.title() != null && !it.title().isBlank()) ? it.title() : it.type().getDescription();
        }

        // 출석/연속출석/일일/주간/이벤트 등: enum description
        return it.type().getDescription();
    }

    private BooleanExpression applyTab(QWalletHistory wh, WalletHistoryListRequestDto.Tab tab) {
        if (tab == null || tab == WalletHistoryListRequestDto.Tab.ALL) return null;
        if (tab == WalletHistoryListRequestDto.Tab.EARN) return wh.amount.gt(0);
        if (tab == WalletHistoryListRequestDto.Tab.USE) return wh.amount.lt(0);
        return null;
    }

    /**
     * ✅ 적립 탭에서만 미션/출석 필터 적용
     * - 미션: MISSION + MISSION_COMPLETE
     * - 출석: ATTENDANCE + ATTENDANCE_STREAK_BONUS
     */
    private BooleanExpression applyEarnSource(QWalletHistory wh,
                                              WalletHistoryListRequestDto.Tab tab,
                                              WalletHistoryListRequestDto.EarnSource earnSource) {
        if (tab != WalletHistoryListRequestDto.Tab.EARN) return null;
        if (earnSource == null || earnSource == WalletHistoryListRequestDto.EarnSource.ALL) return null;

        return switch (earnSource) {
            case MISSION -> wh.type.in(TransactionType.MISSION, TransactionType.MISSION_COMPLETE);
            case ATTENDANCE -> wh.type.in(TransactionType.ATTENDANCE, TransactionType.ATTENDANCE_STREAK_BONUS);
            default -> null;
        };
    }

    private BooleanExpression applyPeriod(QWalletHistory wh, LocalDateTime startAt) {
        if (startAt == null) return null;
        return wh.createdAt.goe(startAt);
    }

    private LocalDateTime resolveStartAt(WalletHistoryListRequestDto.Period period) {
        if (period == null || period == WalletHistoryListRequestDto.Period.ALL) return null;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime raw = switch (period) {
            case THREE_MONTHS -> now.minusMonths(3);
            case SIX_MONTHS -> now.minusMonths(6);
            default -> null;
        };
        if (raw == null) return null;

        return raw.toLocalDate().atTime(LocalTime.MIN);
    }
}
