package com.b1a4.cafeOn.dto.post;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostDetailResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String authorNickname;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long viewCount;
    // fixme: PostListResponseDTO와 같은 이유
    // private List<CommentDTO> comment;
}
