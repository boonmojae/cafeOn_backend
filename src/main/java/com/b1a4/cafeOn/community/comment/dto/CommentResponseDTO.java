package com.b1a4.cafeOn.community.comment.dto;

import com.b1a4.cafeOn.community.comment.entity.CommentEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDTO {

    private Long commentId;
    private Long parentId;
    private Long postId;
    private String authorName;
    private String content;
    private LocalDateTime createdAt;
    private List<CommentResponseDTO> children;
    // private long likeCount;

    public static CommentResponseDTO from(CommentEntity comment) {
        return CommentResponseDTO.builder()
                .commentId(comment.getCommentId())
                .parentId(comment.getParent() != null ? comment.getParent().getCommentId() : null)
                .postId(comment.getPost().getPostId())
                .content(comment.getContent())
                .authorName(comment.getUser().getNickname())
                .createdAt(comment.getCreatedAt())
                .children(new ArrayList<>())
                .build();
    }


}
