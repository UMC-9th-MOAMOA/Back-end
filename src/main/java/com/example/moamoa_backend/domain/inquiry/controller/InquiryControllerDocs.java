package com.example.moamoa_backend.domain.inquiry.controller;

import com.example.moamoa_backend.domain.inquiry.dto.*;
import com.example.moamoa_backend.domain.inquiry.enums.InquiryCategory;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Inquiry API", description = "1:1 문의 관련 API")
public interface InquiryControllerDocs {
    @Operation(
            summary = "1:1 문의 신청",
            description = """
                    multipart/form-data로 문의 정보 + 이미지 파일을 업로드합니다.<br><br>

                    **[인증 필요]**<br>
                    Authorization: Bearer {accessToken}<br><br>

                    **[요청 형식]**<br>
                    - ModelAttribute(Form) 기반<br>
                    - images는 선택값(없어도 요청 가능)<br>
                    """
    )
    ApiResponse<InquiryResponseDTO.CreateResult> createInquiry(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute InquiryFormDTO.Create form
    );

    @Operation(
            summary = "나의 문의 목록 조회",
            description = """
                    기간/카테고리/답변상태 조건으로 나의 문의 목록을 조회합니다.<br>
                    커서 기반 무한스크롤 페이징을 지원합니다.<br><br>

                    **[인증 필요]**<br>
                    Authorization: Bearer {accessToken}<br><br>

                    **[페이징]**<br>
                    - size, cursorCreatedAt, cursorId 로 다음 페이지 조회<br>
                    """
    )
    ApiResponse<InquiryQueryResDto.MyInquiryList> getMyInquiries(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @NotNull InquiryQueryReqDto.Period period,
            @RequestParam(defaultValue = "ALL") InquiryQueryReqDto.AnswerStatus answerStatus,
            @RequestParam(required = false) InquiryCategory category,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String cursorCreatedAt,
            @RequestParam(required = false) Long cursorId
    );

    @Operation(
            summary = "나의 문의 상세 조회",
            description = """
                    본인이 작성한 문의 상세(문의/답변/이미지)를 조회합니다.<br><br>

                    **[인증 필요]**<br>
                    Authorization: Bearer {accessToken}
                    """
    )
    ApiResponse<InquiryDetailResDto.MyInquiryDetail> getMyInquiryDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @NotNull Long inquiryId
    );

    @Operation(
            summary = "문의 답변 등록 (관리자)",
            description = """
                    관리자 권한으로 문의 답변을 등록합니다.<br>
                    multipart/form-data로 답변 + 답변 이미지 파일 업로드를 지원합니다.<br><br>

                    **[관리자 인증 필요]**<br>
                    Authorization: Bearer {accessToken}<br>
                    ROLE_ADMIN 권한 필요
                    """
    )
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<InquiryAnswerResponseDto.CreateResult> answerInquiry(
            @PathVariable Long inquiryId,
            @Valid @ModelAttribute InquiryAnswerFormDTO.Create form
    );
}
