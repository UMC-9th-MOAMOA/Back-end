package com.example.moamoa_backend.item.service.query;

import com.example.moamoa_backend.item.dto.HomePocketResponseDto;

public interface MemberHomeQueryService {
    HomePocketResponseDto.Response getHomePocket(Long memberId);
}
