package com.b1a4.cafeOn.community.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CommentLikeResponseDTO", description = "댓글 좋아요 토글 응답 DTO")
public record CommentLikeResponseDTO(

        @Schema(description = "댓글 ID", example = "101")
        Long commentId,

        @Schema(description = "현재 내가 좋아요 상태인지", example = "true")
        boolean liked,

        @Schema(description = "총 좋아요 수", example = "4")
        long likes
) {}
