package com.example.moamoa_backend.inquiry.service.query;

import com.example.moamoa_backend.inquiry.dto.InquiryQueryReqDto;
import com.example.moamoa_backend.inquiry.dto.InquiryQueryResDto;

public interface InquiryQueryService {
    InquiryQueryResDto.MyInquiryList getMyInquiries(Long memberId, InquiryQueryReqDto.MyInquiryList cond);
}
