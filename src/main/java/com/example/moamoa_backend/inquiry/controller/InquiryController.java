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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class InquiryController {

    private final InquiryCommandService inquiryCommandService;
    private final InquiryQueryService inquiryQueryService;

    /**
     * 1:1 문의 신청 (multipart)
     * request(JSON) + images(파일)
     */
    @Operation(summary = "1:1 문의 신청", description = "multipart로 문의 JSON(request) + 이미지(images)를 업로드합니다. (JWT 필요)")
    @PostMapping(value = "/support/inquiries", consumes = "multipart/form-data")
    public ApiResponse<InquiryResponseDTO.CreateResult> createInquiry(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestPart("request") InquiryRequestDTO.Create request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        Long memberId = extractMemberId(userDetails);

        InquiryResponseDTO.CreateResult result =
                inquiryCommandService.create(memberId, request, images);

        return ApiResponse.onSuccess(GeneralSuccessCode.CREATED, result);
    }

    /**
     * 나의 문의 목록 조회
     */
    @Operation(
            summary = "나의 문의 목록 조회",
            description = "기간(1/3/6/12개월), 카테고리, 답변상태 조건으로 나의 문의 목록을 조회합니다. (무한스크롤 커서 페이징, JWT 필요)"
    )
    @GetMapping("/members/me/support/inquiries")
    public ApiResponse<InquiryQueryResDto.MyInquiryList> getMyInquiries(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @NotNull InquiryQueryReqDto.Period period,
            @RequestParam(defaultValue = "ALL") InquiryQueryReqDto.AnswerStatus answerStatus,
            @RequestParam(required = false) InquiryCategory category,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String cursorCreatedAt,
            @RequestParam(required = false) Long cursorId
    ) {
        Long memberId = extractMemberId(userDetails);

        InquiryQueryReqDto.MyInquiryList cond = new InquiryQueryReqDto.MyInquiryList(
                period, category, answerStatus, size, cursorCreatedAt, cursorId
        );

        InquiryQueryResDto.MyInquiryList result =
                inquiryQueryService.getMyInquiries(memberId, cond);

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
    }

    @Operation(summary = "나의 문의 상세 조회", description = "회원이 본인이 작성한 1:1 문의 상세(문의/답변/이미지)를 조회합니다. (JWT 필요)")
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

    /**
     * 문의 답변 등록 (multipart)
     * request(JSON) + images(파일)
     */
    @Operation(summary = "문의 답변 등록", description = "multipart로 답변 JSON(request) + 답변 이미지(images)를 업로드합니다. (관리자 JWT 필요)")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // ✅ SecurityConfig가 hasAuthority(Role.ROLE_ADMIN.name()) 스타일이면 이게 안전
    @PostMapping(value = "/admin/support/inquiries/{inquiryId}/answer", consumes = "multipart/form-data")
    public ApiResponse<InquiryAnswerResponseDto.CreateResult> answerInquiry(
            @PathVariable Long inquiryId,
            @Valid @RequestPart("request") InquiryAnswerRequestDto.CreateAnswer request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        InquiryAnswerResponseDto.CreateResult result =
                inquiryCommandService.answer(inquiryId, request, images);

        return ApiResponse.onSuccess(GeneralSuccessCode.CREATED, result);
    }

    /**
     * Global JwtAuthFilter가 만든 Authentication principal(UserDetails)의 username이 memberId라고 가정
     */
    private Long extractMemberId(UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        try {
            return Long.valueOf(userDetails.getUsername());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("토큰의 사용자 식별자(username)가 숫자 memberId 형식이 아닙니다.");
        }
    }
}