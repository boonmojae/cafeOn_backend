package com.b1a4.cafeOn.image.controller;

import com.b1a4.cafeOn.image.dto.ImageUploadSimpleResponseDTO;
import com.b1a4.cafeOn.image.enums.ImageCategory;
import com.b1a4.cafeOn.image.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final S3Service s3Service;


    @PostMapping("/upload")
    public ResponseEntity<ImageUploadSimpleResponseDTO> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            S3Service.UploadedImageInfo uploaded =
                    s3Service.uploadImage(file, ImageCategory.POST);

            ImageUploadSimpleResponseDTO body = new ImageUploadSimpleResponseDTO(
                    uploaded.getS3Key(),
                    uploaded.getOriginalFileName(),
                    uploaded.getPublicUrl()
            );

            return ResponseEntity.ok(body);

        } catch (Exception e) {
            log.error("S3 버킷에 이미지 업로드 실패", e);
            return ResponseEntity.badRequest().build();
        }
    }
}