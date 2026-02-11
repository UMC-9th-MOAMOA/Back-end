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
public class WalletHistoryQueryServiceImpl implements WalletHistoryQueryService{
    private final JPAQueryFactory queryFactory;
    private final QWallet wallet = QWallet.wallet;

    @Override
    public WalletHistoryDayResponseDto.Response getDayHistory(Long memberId, LocalDate date) {
        // 해당 날짜의 [00:00, 다음날 00:00) 범위
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        QWalletHistory wh = QWalletHistory.walletHistory;
        QWallet w = QWallet.wallet;
        QMission m = QMission.mission;

        // ✅ WalletHistory + (Mission은 left join)
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

        // totalMinutes: 미션 항목 durationMinutes 합
        int totalMinutes = items.stream()
                .map(WalletHistoryDayResponseDto.Item::missionDurationMinutes)
                .filter(v -> v != null)
                .mapToInt(Integer::intValue)
                .sum();

        // totalAcorns: amount 합
        int totalAcorns = items.stream()
                .mapToInt(WalletHistoryDayResponseDto.Item::amount)
                .sum();

        return new WalletHistoryDayResponseDto.Response(date, items, totalMinutes, totalAcorns);
    }

    @Override
    public WalletPointResponseDto.Response getMyPoint(Long memberId) {
        Integer point = queryFactory
                .select(wallet.point)
                .from(wallet)
                .where(wallet.member.id.eq(memberId))
                .fetchOne();

        return new WalletPointResponseDto.Response(point);
    }
}
