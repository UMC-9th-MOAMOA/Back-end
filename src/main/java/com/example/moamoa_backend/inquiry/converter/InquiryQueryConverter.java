package com.example.moamoa_backend.inquiry.converter;

import com.example.moamoa_backend.inquiry.dto.InquiryQueryResDto;

import java.util.List;

public class InquiryQueryConverter {

    private InquiryQueryConverter() {}

    public static InquiryQueryResDto.MyInquiryList toMyInquiryList(
            List<InquiryQueryResDto.MyInquiryItem> items,
            boolean hasNext
    ) {
        boolean safeHasNext = hasNext && !items.isEmpty();
        InquiryQueryResDto.Cursor nextCursor = null;
        if (safeHasNext) {
            var last = items.get(items.size() - 1);
            nextCursor = new InquiryQueryResDto.Cursor(last.createdAt(), last.inquiryId());
        }
        return new InquiryQueryResDto.MyInquiryList(items, safeHasNext, nextCursor);
    }
}
