package com.b1a4.cafeOn.dto.post;

import com.b1a4.cafeOn.enums.PostType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostListResponseDTO {
    private Long id;
    private PostType type;
    private String title;
    private String authorNickname;
    private LocalDateTime createdAt;
    // fixme: CommentEntity 추가할때 주석 해제
    // private int commentCount;
}
