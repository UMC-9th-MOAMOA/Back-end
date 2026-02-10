package com.example.moamoa_backend.domain.inquiry.service.command;

import com.example.moamoa_backend.domain.inquiry.dto.InquiryAnswerRequestDto;
import com.example.moamoa_backend.domain.inquiry.dto.InquiryAnswerResponseDto;
import com.example.moamoa_backend.domain.inquiry.dto.InquiryRequestDTO;
import com.example.moamoa_backend.domain.inquiry.dto.InquiryResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface InquiryCommandService {
    InquiryResponseDTO.CreateResult create(Long memberId, InquiryRequestDTO.Create request, List<MultipartFile> images);
    InquiryAnswerResponseDto.CreateResult answer(Long inquiryId, InquiryAnswerRequestDto.CreateAnswer request, List<MultipartFile> images);
}
