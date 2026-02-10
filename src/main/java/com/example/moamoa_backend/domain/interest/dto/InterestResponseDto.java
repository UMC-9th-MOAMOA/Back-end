package com.example.moamoa_backend.interest.dto;

public record InterestResponseDto(Long id, String name) {
	public static InterestResponseDto from(Long id, String name) {
		return new InterestResponseDto(id, name);
	}
}
