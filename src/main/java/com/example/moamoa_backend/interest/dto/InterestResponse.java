package com.example.moamoa_backend.interest.dto;

public record InterestResponse(Long id, String name) {
	public static InterestResponse from(Long id, String name) {
		return new InterestResponse(id, name);
	}
}
