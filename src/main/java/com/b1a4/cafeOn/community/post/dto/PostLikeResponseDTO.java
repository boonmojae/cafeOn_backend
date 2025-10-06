package com.b1a4.cafeOn.community.post.dto;

public record PostLikeResponseDTO(
        Long postId,
        boolean liked,
        long likes
) {

}
