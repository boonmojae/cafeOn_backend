package com.b1a4.cafeOn.community.post.dto;

import com.b1a4.cafeOn.image.service.S3Service;
import com.b1a4.cafeOn.image.service.UploadedImageInfo;

import java.util.List;

public record PostUpdateRequest(
        PostRequestDTO postRequestDTO,
        List<UploadedImageInfo> newImages
) {}