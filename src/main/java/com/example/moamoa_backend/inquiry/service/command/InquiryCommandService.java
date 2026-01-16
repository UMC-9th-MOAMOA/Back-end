package com.example.moamoa_backend.inquiry.service.command;

import com.example.moamoa_backend.inquiry.dto.InquiryRequestDTO;
import com.example.moamoa_backend.inquiry.dto.InquiryResponseDTO;

public interface InquiryCommandService {
    InquiryResponseDTO.CreateResult create(Long memberId, InquiryRequestDTO.Create request);
}
