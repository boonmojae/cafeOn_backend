package com.b1a4.cafeOn.image.dto;

public record ImageUploadSimpleResponseDTO(
        String s3Key,
        String originalFileName,
        String url
) {}
