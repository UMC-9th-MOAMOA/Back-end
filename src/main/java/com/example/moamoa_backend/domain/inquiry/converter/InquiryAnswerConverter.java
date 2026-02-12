package com.example.moamoa_backend.domain.inquiry.converter;

import com.example.moamoa_backend.domain.inquiry.dto.InquiryAnswerResponseDto;
import com.example.moamoa_backend.domain.inquiry.entity.AnswerImage;
import com.example.moamoa_backend.domain.inquiry.entity.Inquiry;

import java.util.List;

public class InquiryAnswerConverter {

	public static InquiryAnswerResponseDto.CreateResult toCreateResult(Inquiry inquiry, List<String> urls) {
		List<String> safeUrls = (urls == null)
			? List.of()
			: urls.stream().filter(url -> url != null && !url.isBlank()).toList();
		return new InquiryAnswerResponseDto.CreateResult(
			inquiry.getId(),
			inquiry.getAnswer(),
			inquiry.getAnsweredAt(),
			safeUrls
		);
	}

	@Deprecated(forRemoval = true)
	public static List<AnswerImage> toAnswerImages(List<String> urls) {
		if (urls == null || urls.isEmpty())
			return List.of();
		return urls.stream()
			.filter(url -> url != null && !url.isBlank())
			.map(url -> AnswerImage.builder()
				.imageUrl(url)
				.build())
			.toList();
	}
}
