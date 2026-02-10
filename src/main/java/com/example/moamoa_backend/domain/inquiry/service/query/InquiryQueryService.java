package com.example.moamoa_backend.domain.inquiry.service.query;

import com.example.moamoa_backend.domain.inquiry.dto.InquiryDetailResDto;
import com.example.moamoa_backend.domain.inquiry.dto.InquiryQueryReqDto;
import com.example.moamoa_backend.domain.inquiry.dto.InquiryQueryResDto;

public interface InquiryQueryService {
    InquiryQueryResDto.MyInquiryList getMyInquiries(Long memberId, InquiryQueryReqDto.MyInquiryList cond);
    InquiryDetailResDto.MyInquiryDetail getMyInquiryDetail(Long memberId, Long inquiryId);
}
