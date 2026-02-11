package com.example.moamoa_backend.domain.inquiry.dto;

import com.example.moamoa_backend.domain.inquiry.enums.InquiryCategory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class InquiryFormDTO {

	@Schema(name = "InquiryCreateForm")
	public record Create(
		@NotNull InquiryCategory category,
		@NotBlank @Size(max = 20) String title,
		@NotBlank @Size(max = 2000) String content,

		@Schema(
			description = "첨부 이미지 파일들 (선택, 여러 개 가능)",
			type = "string",
			format = "binary"
		)
		List<MultipartFile> images
	) {
	}
}
