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

    @Value("${aws.s3.public-base-url}")
    private String publicBaseUrl;

    public String upload(MultipartFile file, String dir) throws IOException {
        if (file == null || file.isEmpty()) return null;

        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String safeName = URLEncoder.encode(original, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        String key = dir + "/" + UUID.randomUUID() + "_" + safeName;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(
                request,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        // ✅ 여기서 URL 조립
        return publicBaseUrl + "/" + key;
    }
}
