package com.example.moamoa_backend.interest.dto;

public record SubInterestResponse(Long id, String name) {
	public static SubInterestResponse from(Long id, String name) {
		return new SubInterestResponse(id, name);
	}
}
