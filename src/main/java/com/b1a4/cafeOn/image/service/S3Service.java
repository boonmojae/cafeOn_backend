package com.b1a4.cafeOn.image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.b1a4.cafeOn.image.enums.ImageCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    // 업로드 후 컨트롤러/서비스로 돌려줄 정보
    public static class UploadedImageInfo {
        private final String s3Key;             // S3에 실제로 저장된 key
        private final String originalFileName;  // 사용자가 업로드한 원본 파일명
        private final String contentType;       // MIME type
        private final long sizeBytes;           // 파일 크기
        private final String publicUrl;         // 접근 URL (버킷 공개 정책 따라 다름)

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

    // category: POST / REVIEW / CHAT 중 어디에 쓰는 이미지인지
    public UploadedImageInfo uploadImage(MultipartFile file, ImageCategory category) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다.");
        }

        // 원본 파일명 확보
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "unnamed";
        }

        // 용도별 prefix 결정 (S3 내에서 '폴더'처럼 쓰임)
        String prefix = switch (category) {
            case POST -> "posts/";
            case REVIEW -> "reviews/";
            case CHAT -> "chats/";
        };

        // 중복 방지 UUID
        String uuid = UUID.randomUUID().toString();
        String key = prefix + uuid + "_" + originalName;

        // S3 메타데이터
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try {
            amazonS3.putObject(bucket, key, file.getInputStream(), metadata);
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 중 오류가 발생했습니다.", e);
        }

        // 퍼블릭 URL (버킷 퍼블릭일 경우 바로 접근 가능)
        String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
        String url = "https://" + bucket + ".s3.amazonaws.com/" + encodedKey;

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
}
