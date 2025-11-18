package com.b1a4.cafeOn.image.service;

import com.b1a4.cafeOn.image.enums.ImageCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cafeon.cdn-base-url}")
    private String cdnBaseUrl;

    // category: POST / REVIEW / CHAT / PROFILE
    public UploadedImageInfo uploadImage(MultipartFile file, ImageCategory category) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다.");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) originalName = "unnamed";

        String prefix = switch (category) {
            case POST -> "posts/";
            case REVIEW -> "reviews/";
            case CHAT -> "chats/";
            case PROFILE -> "profiles/";
        };

        String uuid = UUID.randomUUID().toString();
        String key = prefix + uuid + "_" + originalName;

        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        try {
            s3.putObject(putReq, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 중 오류가 발생했습니다.", e);
        }

        // 퍼블릭 URL 생성
        String base = cdnBaseUrl.endsWith("/") ? cdnBaseUrl : cdnBaseUrl + "/";
        String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
        String url = base + encodedKey;

        return new UploadedImageInfo(
                key,
                originalName,
                file.getContentType(),
                file.getSize(),
                url
        );
    }

    // 삭제 (단건)
    public void deleteImageByKey(String s3Key) {
        s3.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .build());
    }

    // 삭제 (대량)
    public void deleteAll(List<String> keys) {
        if (keys == null || keys.isEmpty()) return;

        final int BATCH = 1000;
        for (int start = 0; start < keys.size(); start += BATCH) {
            int end = Math.min(start + BATCH, keys.size());
            var batch = keys.subList(start, end);

            var objects = batch.stream()
                    .map(k -> ObjectIdentifier.builder().key(k).build())
                    .toList();

            s3.deleteObjects(DeleteObjectsRequest.builder()
                    .bucket(bucket)
                    .delete(Delete.builder().objects(objects).build())
                    .build());
        }
    }
}
