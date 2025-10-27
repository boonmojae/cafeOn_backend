package com.b1a4.cafeOn.image.dto;

import com.b1a4.cafeOn.image.entity.ImageEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageResponseDTO {

    private Long imageId;
    private String originalFileName;
    private String imageUrl;
    public static ImageResponseDTO from(ImageEntity image) {
        return ImageResponseDTO.builder()
                .imageId(image.getImageId())
                .originalFileName(image.getOriginalFileName())
                .imageUrl(image.getPublicUrl())
                .build();
    }
}
