package com.b1a4.cafeOn.community.post.dto;

import com.b1a4.cafeOn.image.service.S3Service;

import java.util.List;

public record PostUpdateRequest(
        PostRequestDTO postRequestDTO,
        List<S3Service.UploadedImageInfo> newImages
) {}