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

    // ✅ 권장: https://moamoa-s3-prod-bucket.s3.ap-northeast-2.amazonaws.com (끝에 / 없음, /public 없음)
    @Value("${aws.s3.public-base-url}")
    private String publicBaseUrl;

    public String upload(MultipartFile file, String dir) throws IOException {
        if (file == null || file.isEmpty()) return null;

        String original = file.getOriginalFilename();
        if (original == null || original.isBlank()) original = "file";

        // ✅ 1) key에는 "인코딩된 문자열(%)"을 넣지 말 것
        //    파일명은 최소한만 정리: 경로문자/제어문자 제거 + 공백 정리 정도
        String cleanedName = sanitizeFilename(original);

        // dir 정규화
        String normalizedDir = (dir == null || dir.isBlank()) ? "misc" : dir.strip();
        normalizedDir = normalizedDir.replaceAll("^/+", "").replaceAll("/+$", "");

        // ✅ public prefix 강제
        String key = "public/" + normalizedDir + "/" + UUID.randomUUID() + "_" + cleanedName;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(
                request,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        // ✅ 2) URL로 반환할 때만 "한 번" 인코딩해서 DB에 저장
        String base = normalizeBaseUrl(publicBaseUrl);

        // key는 path이므로 segment 단위 인코딩이 안전하지만,
        // 여기서는 "/"는 살리고 나머지만 인코딩하도록 처리
        String encodedKey = encodeS3KeyForUrl(key);

        return base + "/" + encodedKey;
    }

    // 파일명에서 위험한 문자만 제거/치환 (S3 key raw 저장용)
    private String sanitizeFilename(String name) {
        // 경로 구분자 제거(폴더 탈출 방지)
        String v = name.replace("\\", "_").replace("/", "_");
        // 제어문자 제거
        v = v.replaceAll("[\\p{Cntrl}]", "");
        // 앞뒤 공백 제거
        v = v.trim();
        // 너무 길면 잘라내기(선택)
        if (v.length() > 150) v = v.substring(v.length() - 150);
        if (v.isBlank()) v = "file";
        return v;
    }

    // baseUrl에 /public이 붙어있으면 제거해서 중복 방지
    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null) return "";
        String base = baseUrl.replaceAll("/+$", "");
        // 혹시 설정이 .../public 로 되어있으면 제거해줌 (key에 public/가 이미 있으니까)
        if (base.endsWith("/public")) base = base.substring(0, base.length() - "/public".length());
        return base;
    }

    // S3 key를 URL에서 안전하게 쓰도록 1회 인코딩 (%는 새로 생성되지만, key에는 들어가지 않음)
    private String encodeS3KeyForUrl(String key) {
        // "/"는 경로로 유지하고, 각 조각만 인코딩
        String[] parts = key.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append("/");
            sb.append(URLEncoder.encode(parts[i], StandardCharsets.UTF_8)
                    .replace("+", "%20"));
        }
        return sb.toString();
    }
}