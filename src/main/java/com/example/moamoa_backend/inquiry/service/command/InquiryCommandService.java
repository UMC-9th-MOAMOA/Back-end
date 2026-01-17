package com.example.moamoa_backend.inquiry.service.command;

import com.example.moamoa_backend.inquiry.dto.InquiryAnswerRequestDto;
import com.example.moamoa_backend.inquiry.dto.InquiryAnswerResponseDto;
import com.example.moamoa_backend.inquiry.dto.InquiryRequestDTO;
import com.example.moamoa_backend.inquiry.dto.InquiryResponseDTO;

public interface InquiryCommandService {
    InquiryResponseDTO.CreateResult create(Long memberId, InquiryRequestDTO.Create request);
    InquiryAnswerResponseDto.CreateResult answer(Long inquiryId, InquiryAnswerRequestDto.CreateAnswer request);
}
