package com.example.moamoa_backend.inquiry.dto;

import com.example.moamoa_backend.inquiry.enums.InquiryCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class InquiryRequestDTO {
    public record Create(
            @NotNull InquiryCategory category,
            @NotBlank @Size(max = 20) String title,
            @NotBlank @Size(max = 2000) String content,
            @Size(max = 5) List<@Size(max = 1000) String> imageUrls
    ) {}
}
