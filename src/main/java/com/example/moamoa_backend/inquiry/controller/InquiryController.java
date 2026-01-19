package com.example.moamoa_backend.inquiry.controller;

import com.example.moamoa_backend.global.apiPayload.code.GeneralSuccessCode;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.inquiry.dto.*;
import com.example.moamoa_backend.inquiry.enums.InquiryCategory;
import com.example.moamoa_backend.inquiry.service.command.InquiryCommandService;
import com.example.moamoa_backend.inquiry.service.query.InquiryQueryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class InquiryController {

    private final InquiryCommandService inquiryCommandService;
    private final InquiryQueryService inquiryQueryService;

    /**
     * 1:1 문의 신청
     * - JWT 인증 기반 (memberId는 토큰 subject에서 추출)
     */
    @Operation(
            summary = "1:1 문의 신청",
            description = "회원이 고객센터에 1:1 문의를 등록합니다. (JWT 필요)"
    )
    @PostMapping("/support/inquiries")
    public ApiResponse<InquiryResponseDTO.CreateResult> createInquiry(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody InquiryRequestDTO.Create request
    ) {
        Long memberId = extractMemberId(userDetails);

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
    @GetMapping("/members/me/support/inquiries")
    public ApiResponse<InquiryQueryResDto.MyInquiryList> getMyInquiries(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @NotNull InquiryQueryReqDto.Period period,                          // P1M, P3M, P6M, P1Y
            @RequestParam(defaultValue = "ALL") InquiryQueryReqDto.AnswerStatus answerStatus, // ALL, COMPLETED, PENDING
            @RequestParam(required = false) InquiryCategory category,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String cursorCreatedAt, // ISO-8601: 2026-01-17T12:34:56
            @RequestParam(required = false) Long cursorId
    ) {
        Long memberId = extractMemberId(userDetails);

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
    @GetMapping("/members/me/support/inquiries/{inquiryId}")
    public ApiResponse<InquiryDetailResDto.MyInquiryDetail> getMyInquiryDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @NotNull Long inquiryId
    ) {
        Long memberId = extractMemberId(userDetails);

        InquiryDetailResDto.MyInquiryDetail result =
                inquiryQueryService.getMyInquiryDetail(memberId, inquiryId);

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
    }

    @Operation(
            summary = "문의 답변 등록",
            description = "고객센터(관리자/담당자)가 문의에 답변을 등록하고 답변 이미지를 함께 저장합니다."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/support/inquiries/{inquiryId}/answer")
    public ApiResponse<InquiryAnswerResponseDto.CreateResult> answerInquiry(
            @PathVariable Long inquiryId,
            @Valid @RequestBody InquiryAnswerRequestDto.CreateAnswer request
    ) {
        InquiryAnswerResponseDto.CreateResult result =
                inquiryCommandService.answer(inquiryId, request);

        return ApiResponse.onSuccess(GeneralSuccessCode.CREATED, result);
    }

    /**
     * Global JwtUtil#getAuthentication()에서 principal(UserDetails)의 username에 memberId를 넣고 있으므로
     * 동일 규칙으로 memberId를 뽑아쓴다.
     */
    private Long extractMemberId(UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        return Long.valueOf(userDetails.getUsername());
    }
}