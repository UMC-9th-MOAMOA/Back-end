package com.example.moamoa_backend.domain.inquiry.controller;

import com.example.moamoa_backend.domain.inquiry.dto.InquiryAnswerFormDTO;
import com.example.moamoa_backend.domain.inquiry.dto.InquiryAnswerRequestDto;
import com.example.moamoa_backend.domain.inquiry.dto.InquiryAnswerResponseDto;
import com.example.moamoa_backend.domain.inquiry.dto.InquiryDetailResDto;
import com.example.moamoa_backend.domain.inquiry.dto.InquiryFormDTO;
import com.example.moamoa_backend.domain.inquiry.dto.InquiryQueryReqDto;
import com.example.moamoa_backend.domain.inquiry.dto.InquiryQueryResDto;
import com.example.moamoa_backend.domain.inquiry.dto.InquiryRequestDTO;
import com.example.moamoa_backend.domain.inquiry.dto.InquiryResponseDTO;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.domain.inquiry.dto.*;
import com.example.moamoa_backend.domain.inquiry.enums.InquiryCategory;
import com.example.moamoa_backend.domain.inquiry.exception.code.InquirySuccessCode;
import com.example.moamoa_backend.domain.inquiry.service.command.InquiryCommandService;
import com.example.moamoa_backend.domain.inquiry.service.query.InquiryQueryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

public interface InquiryControllerDocs {
    @Operation(
            summary = "1:1 문의 신청",
            description = "multipart/form-data 폼 객체로 문의 정보 + 이미지 파일을 업로드합니다. (JWT 필요)"
    )
    public ApiResponse<InquiryResponseDTO.CreateResult> createInquiry(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute InquiryFormDTO.Create form
    ) ;

    @Operation(
            summary = "나의 문의 목록 조회",
            description = "기간(1/3/6/12개월), 카테고리, 답변상태 조건으로 나의 문의 목록을 조회합니다. (무한스크롤 커서 페이징, JWT 필요)"
    )
    public ApiResponse<InquiryQueryResDto.MyInquiryList> getMyInquiries(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @NotNull InquiryQueryReqDto.Period period,
            @RequestParam(defaultValue = "ALL") InquiryQueryReqDto.AnswerStatus answerStatus,
            @RequestParam(required = false) InquiryCategory category,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String cursorCreatedAt,
            @RequestParam(required = false) Long cursorId
    ) ;

    @Operation(summary = "나의 문의 상세 조회", description = "회원이 본인이 작성한 1:1 문의 상세(문의/답변/이미지)를 조회합니다. (JWT 필요)")
    public ApiResponse<InquiryDetailResDto.MyInquiryDetail> getMyInquiryDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @NotNull Long inquiryId
    ) ;

    @Operation(summary = "문의 답변 등록", description = "multipart/form-data 폼 객체로 답변 + 답변 이미지 파일을 업로드합니다. (관리자 JWT 필요)")
    public ApiResponse<InquiryAnswerResponseDto.CreateResult> answerInquiry(
            @PathVariable Long inquiryId,
            @Valid @ModelAttribute InquiryAnswerFormDTO.Create form
    ) ;

}
