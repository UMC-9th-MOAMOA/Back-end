package com.example.moamoa_backend.domain.inquiry.repository;

import com.example.moamoa_backend.domain.inquiry.dto.InquiryDetailResDto;
import com.example.moamoa_backend.domain.inquiry.dto.InquiryQueryReqDto;
import com.example.moamoa_backend.domain.inquiry.dto.InquiryQueryResDto;

import java.util.List;

public interface InquiryRepositoryCustom {
    List<InquiryQueryResDto.MyInquiryItem> findMyInquiryItems(
            Long memberId,
            InquiryQueryReqDto.MyInquiryList cond
    );
    InquiryDetailResDto.MyInquiryDetail findMyInquiryDetail(Long memberId, Long inquiryId);
}
