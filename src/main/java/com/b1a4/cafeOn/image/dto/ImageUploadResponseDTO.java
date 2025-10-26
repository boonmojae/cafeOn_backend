package com.b1a4.cafeOn.image.dto;

import com.b1a4.cafeOn.image.entity.ImageEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageUploadResponseDTO {

    private Long imageId;
    private String originalFileName;
    private String imageUrl;

    public static ImageUploadResponseDTO of(ImageEntity image, String publicUrl) {
        return ImageUploadResponseDTO.builder()
                .imageId(image.getImageId())
                .originalFileName(image.getOriginalFileName())
                .imageUrl(publicUrl)
                .build();
    }

}
