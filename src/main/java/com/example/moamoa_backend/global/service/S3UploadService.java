package com.example.moamoa_backend.global.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3UploadService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    // 예: https://moamoa-s3-prod-bucket.s3.ap-northeast-2.amazonaws.com/public
    @Value("${aws.s3.public-base-url}")
    private String publicBaseUrl;

    /**
     * @param dir public 아래 상대 경로 (예: "user/123", "inquiries", "inquiries/answers")
     *            "public/" prefix는 자동으로 붙임
     */
    public String upload(MultipartFile file, String dir) throws IOException {
        if (file == null || file.isEmpty()) return null;

        String original = file.getOriginalFilename();
        if (original == null || original.isBlank()) original = "file";

        // 파일명 안전화
        String safeName = URLEncoder.encode(original, StandardCharsets.UTF_8)
                .replace("+", "%20");

        // dir 정규화
        String normalizedDir = (dir == null || dir.isBlank()) ? "misc" : dir.strip();
        normalizedDir = normalizedDir.replaceAll("^/+", "").replaceAll("/+$", "");

        // ✅ public prefix 강제 (버킷 정책 public/* 와 일치)
        String key = "public/" + normalizedDir + "/" + UUID.randomUUID() + "_" + safeName;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        // ✅ baseUrl도 안전하게 (뒤 슬래시 제거)
        String base = publicBaseUrl.replaceAll("/+$", "");

        return base + "/" + key;
    }
}
