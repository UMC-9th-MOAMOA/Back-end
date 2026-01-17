package com.example.moamoa_backend.inquiry.controller;


import com.example.moamoa_backend.global.apiPayload.code.GeneralSuccessCode;
import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.inquiry.dto.InquiryRequestDTO;
import com.example.moamoa_backend.inquiry.dto.InquiryResponseDTO;
import com.example.moamoa_backend.inquiry.service.command.InquiryCommandService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class InquiryController {

    private final InquiryCommandService inquiryCommandService;

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
}
