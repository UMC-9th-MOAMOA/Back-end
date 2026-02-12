package com.example.moamoa_backend.domain.inquiry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public class InquiryAnswerRequestDto {
	public record CreateAnswer(
		@NotBlank @Size(max = 2000)
		String answer,
		@NotBlank @Size(max = 30) String responderName
	) {
	}
}
