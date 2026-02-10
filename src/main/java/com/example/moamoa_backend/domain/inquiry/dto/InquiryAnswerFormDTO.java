package com.example.moamoa_backend.domain.inquiry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class InquiryAnswerFormDTO {

    @Schema(name = "InquiryAnswerCreateForm")
    public record Create(
            @NotBlank @Size(max = 2000) String answer,

            @Schema(
                    description = "답변 이미지 파일들 (선택, 여러 개 가능)",
                    type = "string",
                    format = "binary"
            )
            List<MultipartFile> images
    ) {}
}
