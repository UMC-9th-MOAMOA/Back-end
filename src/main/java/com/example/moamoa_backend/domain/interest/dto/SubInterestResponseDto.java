package com.example.moamoa_backend.domain.interest.dto;

public record SubInterestResponseDto(Long id, String name) {
	public static SubInterestResponseDto from(Long id, String name) {
		return new SubInterestResponseDto(id, name);
	}
}
