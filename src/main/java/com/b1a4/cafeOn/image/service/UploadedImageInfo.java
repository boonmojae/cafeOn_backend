package com.b1a4.cafeOn.image.service;

import lombok.Getter;

@Getter
public class UploadedImageInfo {

    private final String s3Key;
    private String originalFileName;
    private final String contentType;
    private final long sizeBytes;
    private final String publicUrl;

    public UploadedImageInfo(String s3Key, String originalFileName, String contentType, long sizeBytes, String publicUrl) {
        this.s3Key = s3Key;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.publicUrl = publicUrl;
    }


}
