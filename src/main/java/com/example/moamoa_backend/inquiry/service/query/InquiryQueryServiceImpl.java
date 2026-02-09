package com.example.moamoa_backend.inquiry.service.query;

import com.example.moamoa_backend.inquiry.converter.InquiryQueryConverter;
import com.example.moamoa_backend.inquiry.dto.InquiryDetailResDto;
import com.example.moamoa_backend.inquiry.dto.InquiryQueryReqDto;
import com.example.moamoa_backend.inquiry.dto.InquiryQueryResDto;
import com.example.moamoa_backend.inquiry.repository.InquiryRepository;
import com.example.moamoa_backend.inquiry.repository.InquiryRepositoryCustom;
import com.example.moamoa_backend.member.exception.MemberException;
import com.example.moamoa_backend.member.exception.code.MemberErrorCode;
import com.example.moamoa_backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryQueryServiceImpl implements com.example.moamoa_backend.inquiry.service.query.InquiryQueryService{

    private final InquiryRepository inquiryRepository;
    private final MemberRepository memberRepository;

    @Override
    public InquiryQueryResDto.MyInquiryList getMyInquiries(Long memberId, InquiryQueryReqDto.MyInquiryList cond) {

        if (!memberRepository.existsById(memberId)) {
            throw new MemberException(MemberErrorCode.MEMBER_NOT_FOUND);
        }
        List<InquiryQueryResDto.MyInquiryItem> fetched = inquiryRepository.findMyInquiryItems(memberId, cond);

        int size = (cond.size() == null || cond.size() <= 0) ? 10 : Math.min(cond.size(), 50);

        boolean hasNext = fetched.size() > size;
        List<InquiryQueryResDto.MyInquiryItem> items = hasNext ? fetched.subList(0, size) : fetched;

        // contentPreview 가공 (너 UI 리스트용)
        List<InquiryQueryResDto.MyInquiryItem> refined = items.stream()
                .map(i -> new InquiryQueryResDto.MyInquiryItem(
                        i.inquiryId(),
                        i.category(),
                        i.title(),
                        toPreview(i.contentPreview(), 40),  // 40자 미리보기
                        i.answered(),
                        i.createdAt(),
                        i.responderName(),
                        toPreview(i.answerPreview(), 40)
                ))
                .toList();

        return InquiryQueryConverter.toMyInquiryList(refined, hasNext);
    }

    private String toPreview(String content, int maxLen) {
        if (content == null) return null;
        String trimmed = content.trim();
        if (trimmed.length() <= maxLen) return trimmed;
        return trimmed.substring(0, maxLen) + "...";
    }

    @Override
    public InquiryDetailResDto.MyInquiryDetail getMyInquiryDetail(Long memberId, Long inquiryId) {

        // ✅ memberId 존재 검증 (원하면 빼도 되지만 지금 정책상 넣는 게 좋음)
        if (!memberRepository.existsById(memberId)) {
            throw new MemberException(MemberErrorCode.MEMBER_NOT_FOUND);
        }

        return inquiryRepository.findMyInquiryDetail(memberId, inquiryId);
    }
}
