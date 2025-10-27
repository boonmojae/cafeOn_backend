package com.b1a4.cafeOn.community.comment.dto;

import com.b1a4.cafeOn.community.comment.entity.CommentEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.b1a4.cafeOn.common.DisplayMasking.nicknameOf;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDTO {

    private Long commentId;
    private Long parentId;
    private Long postId;
    private String authorNickname;
    private String content;
    private LocalDateTime createdAt;

    @Builder.Default
    private List<CommentResponseDTO> children = new ArrayList<>();

    private long likeCount;
    private boolean likedByMe;

    // 기본형(기존 코드 호환)
    public static CommentResponseDTO from(CommentEntity comment) {
        return from(comment, 0L, false);
    }

    // 좋아요 정보 포함(목록/트리에서 사용)
    public static CommentResponseDTO from(CommentEntity comment, long likeCount, boolean likedByMe) {

        String nicknameToDisplay = nicknameOf(comment.getUser());

        return CommentResponseDTO.builder()
                .commentId(comment.getCommentId())
                .parentId(comment.getParent() != null ? comment.getParent().getCommentId() : null)
                .postId(comment.getPost().getPostId())
                .content(comment.getContent())
                .authorNickname(nicknameToDisplay)
                .createdAt(comment.getCreatedAt())
                .likeCount(likeCount)
                .likedByMe(likedByMe)
                .build();
    }
}
