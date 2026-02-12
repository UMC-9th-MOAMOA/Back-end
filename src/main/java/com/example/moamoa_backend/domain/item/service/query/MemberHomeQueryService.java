package com.example.moamoa_backend.domain.item.service.query;

import com.example.moamoa_backend.domain.item.dto.HomePocketResponseDto;

public interface MemberHomeQueryService {
	HomePocketResponseDto.Response getHomePocket(Long memberId);
}
