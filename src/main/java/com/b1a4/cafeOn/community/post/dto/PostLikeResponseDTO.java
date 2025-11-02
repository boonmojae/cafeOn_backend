package com.b1a4.cafeOn.community.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PostLikeResponseDTO", description = "게시글 좋아요 토글 응답 DTO")
public record PostLikeResponseDTO(

        @Schema(description = "게시글 ID", example = "123")
        Long postId,

        @Schema(description = "현재 내가 좋아요 상태인지", example = "true")
        boolean liked,

        @Schema(description = "총 좋아요 수", example = "42")
        long likes
) {}
