package com.example.moamoa_backend.inquiry.repository;

import com.example.moamoa_backend.inquiry.dto.InquiryQueryReqDto;
import com.example.moamoa_backend.inquiry.dto.InquiryQueryResDto;
import com.example.moamoa_backend.inquiry.entity.QInquiry;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.core.types.Projections;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class InquiryRepositoryImpl implements InquiryRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<InquiryQueryResDto.MyInquiryItem> findMyInquiryItems(Long memberId, InquiryQueryReqDto.MyInquiryList cond) {
        QInquiry inquiry = QInquiry.inquiry;

        int size = (cond.size() == null || cond.size() <= 0) ? 10 : Math.min(cond.size(), 50);

        LocalDateTime from = LocalDateTime.now().minusMonths(cond.period().months());

        // 커서 파싱
        LocalDateTime cursorCreatedAt = parseDateTime(cond.cursorCreatedAt());
        Long cursorId = cond.cursorId();

        return queryFactory
                .select(Projections.constructor(
                        InquiryQueryResDto.MyInquiryItem.class,
                        inquiry.id,
                        inquiry.category,
                        inquiry.title,
                        // contentPreview: 길면 잘라서 (DB 함수 싫으면 서비스에서 잘라도 됨)
                        inquiry.content,
                        // answered: answeredAt != null 로 판단
                        inquiry.answeredAt.isNotNull(),
                        inquiry.createdAt
                ))
                .from(inquiry)
                .where(
                        inquiry.member.id.eq(memberId),
                        inquiry.createdAt.goe(from),
                        categoryEq(cond),
                        answerStatusFilter(cond),
                        cursorCondition(inquiry, cursorCreatedAt, cursorId)
                )
                .orderBy(inquiry.createdAt.desc(), inquiry.id.desc())
                .limit(size + 1) // hasNext 확인용
                .fetch();
    }

    private BooleanExpression categoryEq(InquiryQueryReqDto.MyInquiryList cond) {
        if (cond.category() == null) return null;
        return QInquiry.inquiry.category.eq(cond.category());
    }

    private BooleanExpression answerStatusFilter(InquiryQueryReqDto.MyInquiryList cond) {
        QInquiry inquiry = QInquiry.inquiry;
        if (cond.answerStatus() == null || cond.answerStatus() == InquiryQueryReqDto.AnswerStatus.ALL) return null;
        if (cond.answerStatus() == InquiryQueryReqDto.AnswerStatus.COMPLETED) {
            return inquiry.answeredAt.isNotNull();
        }
        return inquiry.answeredAt.isNull();
    }

    /**
     * keyset pagination:
     * (createdAt < cursorCreatedAt) OR (createdAt = cursorCreatedAt AND id < cursorId)
     */
    private BooleanExpression cursorCondition(QInquiry inquiry, LocalDateTime cursorCreatedAt, Long cursorId) {
        if (cursorCreatedAt == null || cursorId == null) return null;

        return inquiry.createdAt.lt(cursorCreatedAt)
                .or(inquiry.createdAt.eq(cursorCreatedAt).and(inquiry.id.lt(cursorId)));
    }

    private LocalDateTime parseDateTime(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDateTime.parse(s);
        } catch (DateTimeParseException e) {
            return null; // 파싱 실패면 커서 무시
        }
    }
}
