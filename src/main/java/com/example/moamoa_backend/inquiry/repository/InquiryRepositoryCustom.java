package com.example.moamoa_backend.inquiry.repository;

import com.example.moamoa_backend.inquiry.dto.InquiryQueryReqDto;
import com.example.moamoa_backend.inquiry.dto.InquiryQueryResDto;

import java.util.List;

public interface InquiryRepositoryCustom {
    List<InquiryQueryResDto.MyInquiryItem> findMyInquiryItems(
            Long memberId,
            InquiryQueryReqDto.MyInquiryList cond
    );
}
