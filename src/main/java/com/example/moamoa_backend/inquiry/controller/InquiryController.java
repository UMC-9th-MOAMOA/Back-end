package com.example.moamoa_backend.inquiry.controller;

import com.example.moamoa_backend.global.apiPayload.code.GeneralSuccessCode;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.inquiry.dto.*;
import com.example.moamoa_backend.inquiry.enums.InquiryCategory;
import com.example.moamoa_backend.inquiry.service.command.InquiryCommandService;
import com.example.moamoa_backend.inquiry.service.query.InquiryQueryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class InquiryController {

    private final InquiryCommandService inquiryCommandService;
    private final InquiryQueryService inquiryQueryService;

    /**
     * 1:1 문의 신청
     * (현재는 JWT 없이 memberId 직접 전달)
     */
    @Operation(
            summary = "1:1 문의 신청",
            description = "회원이 고객센터에 1:1 문의를 등록합니다."
    )
    @PostMapping("/support/inquiries")
    public ApiResponse<InquiryResponseDTO.CreateResult> createInquiry(
            @RequestParam Long memberId,
            @Valid @RequestBody InquiryRequestDTO.Create request
    ) {
        InquiryResponseDTO.CreateResult result =
                inquiryCommandService.create(memberId, request);

        return ApiResponse.onSuccess(GeneralSuccessCode.CREATED, result);
    }

    /**
     * 나의 문의 목록 조회 (QueryDSL + 커서 페이징)
     */
    @Operation(
            summary = "나의 문의 목록 조회",
            description = "기간(1/3/6/12개월), 카테고리, 답변상태 조건으로 나의 문의 목록을 조회합니다. (무한스크롤 커서 페이징)"
    )
    @GetMapping("/members/mesupport/inquiries")
    public ApiResponse<InquiryQueryResDto.MyInquiryList> getMyInquiries(
            @RequestParam Long memberId,
            @RequestParam InquiryQueryReqDto.Period period,                         // P1M, P3M, P6M, P1Y
            @RequestParam(defaultValue = "ALL") InquiryQueryReqDto.AnswerStatus answerStatus, // ALL, COMPLETED, PENDING
            @RequestParam(required = false) InquiryCategory category,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String cursorCreatedAt, // ISO-8601: 2026-01-17T12:34:56
            @RequestParam(required = false) Long cursorId
    ) {
        InquiryQueryReqDto.MyInquiryList cond = new InquiryQueryReqDto.MyInquiryList(
                period,
                category,
                answerStatus,
                size,
                cursorCreatedAt,
                cursorId
        );

        InquiryQueryResDto.MyInquiryList result =
                inquiryQueryService.getMyInquiries(memberId, cond);

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
    }

    @Operation(summary = "나의 문의 상세 조회", description = "회원이 본인이 작성한 1:1 문의 상세(문의/답변/이미지)를 조회합니다.")
    @GetMapping("/members/mesupport/inquiries/{inquiryId}")
    public ApiResponse<InquiryDetailResDto.MyInquiryDetail> getMyInquiryDetail(
            @RequestParam Long memberId,
            @PathVariable Long inquiryId
    ) {
        InquiryDetailResDto.MyInquiryDetail result =
                inquiryQueryService.getMyInquiryDetail(memberId, inquiryId);

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
    }
}
