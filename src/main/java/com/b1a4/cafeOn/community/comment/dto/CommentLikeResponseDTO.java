package com.b1a4.cafeOn.community.comment.dto;

public record CommentLikeResponseDTO(
        Long commentId,
        boolean liked,
        long likes
) {

}
