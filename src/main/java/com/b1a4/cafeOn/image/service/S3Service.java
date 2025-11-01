package com.b1a4.cafeOn.image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.b1a4.cafeOn.image.enums.ImageCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cafeon.cdn-base-url}")
    private String cdnBaseUrl;

    public static class UploadedImageInfo {
        private final String s3Key;             // S3에 실제로 저장된 key (ex. "reviews/uuid_original.jpg")
        private final String originalFileName;  // 원본 파일명
        private final String contentType;       // MIME type
        private final long sizeBytes;           // 파일 크기
        private final String publicUrl;         // 최종 접근 URL (cdnBaseUrl + key)

        public UploadedImageInfo(String s3Key,
                                 String originalFileName,
                                 String contentType,
                                 long sizeBytes,
                                 String publicUrl) {
            this.s3Key = s3Key;
            this.originalFileName = originalFileName;
            this.contentType = contentType;
            this.sizeBytes = sizeBytes;
            this.publicUrl = publicUrl;
        }

        public String getS3Key() { return s3Key; }
        public String getOriginalFileName() { return originalFileName; }
        public String getContentType() { return contentType; }
        public long getSizeBytes() { return sizeBytes; }
        public String getPublicUrl() { return publicUrl; }
    }

    // category: POST / REVIEW / CHAT
    public UploadedImageInfo uploadImage(MultipartFile file, ImageCategory category) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다.");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "unnamed";
        }

        String prefix = switch (category) {
            case POST -> "posts/";
            case REVIEW -> "reviews/";
            case CHAT -> "chats/";
            case PROFILE -> "profiles/";
        };

        // key 생성 (폴더/prefix + uuid + "_" + 원본명)
        String uuid = UUID.randomUUID().toString();
        String key = prefix + uuid + "_" + originalName;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try {
            amazonS3.putObject(bucket, key, file.getInputStream(), metadata);
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 중 오류가 발생했습니다.", e);
        }

        // 퍼블릭 URL 생성
        String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);

        String base = cdnBaseUrl.endsWith("/") ? cdnBaseUrl : cdnBaseUrl + "/";

        String url = base + encodedKey;

        return new UploadedImageInfo(
                key,
                originalName,
                file.getContentType(),
                file.getSize(),
                url
        );
    }


    // 삭제
    public void deleteImageByKey(String s3Key) {
        amazonS3.deleteObject(bucket, s3Key);
    }


    public void deleteAll(List<String> keys) {
        if (keys == null || keys.isEmpty()) return;

        final int BATCH = 1000;
        for (int start = 0; start < keys.size(); start += BATCH) {
            int end = Math.min(start + BATCH, keys.size());
            var batch = keys.subList(start, end);

            DeleteObjectsRequest req = new DeleteObjectsRequest(bucket);
            req.setKeys(batch.stream().map(DeleteObjectsRequest.KeyVersion::new).toList());
            amazonS3.deleteObjects(req);
        }
    }
}

