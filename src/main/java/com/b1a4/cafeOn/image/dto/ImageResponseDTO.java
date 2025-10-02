package com.b1a4.cafeOn.image.dto;

import com.b1a4.cafeOn.image.entity.ImageEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageResponseDTO {

    private Long imageId;
    private String originalFileName;
    private String imageUrl; // 프론트엔드가 사용할 최종 이미지 URL

    public static ImageResponseDTO from(ImageEntity image) {
        return ImageResponseDTO.builder()
                .imageId(image.getImageId())
                .originalFileName(image.getOriginalFileName())
                .imageUrl("/api/posts/images/" + image.getStoredFileName())
                .build();
    }
}
