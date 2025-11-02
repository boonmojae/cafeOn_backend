package com.b1a4.cafeOn.community.comment.dto;

import com.b1a4.cafeOn.community.comment.entity.CommentEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.b1a4.cafeOn.common.DisplayMasking.nicknameOf;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CommentResponseDTO", description = "게시글 댓글 응답 DTO(대댓글 포함)")
public class CommentResponseDTO {

    @Schema(description = "댓글 ID", example = "101")
    private Long commentId;

    @Schema(description = "부모 댓글 ID(최상위면 null)", example = "100", nullable = true)
    private Long parentId;

    @Schema(description = "게시글 ID", example = "55")
    private Long postId;

    @Schema(description = "작성자 닉네임(마스킹 적용)", example = "모짜")
    private String authorNickname;

    @Schema(description = "댓글 내용", example = "좋은 글 감사합니다!")
    private String content;

    @Schema(description = "작성 시각(ISO-8601)", example = "2025-11-02T14:00:00")
    private LocalDateTime createdAt;

    @Builder.Default
    @Schema(description = "대댓글 목록", nullable = true)
    private List<CommentResponseDTO> children = new ArrayList<>();

    @Schema(description = "좋아요 수", example = "3")
    private long likeCount;

    @Schema(description = "내가 좋아요 눌렀는지 여부", example = "false")
    private boolean likedByMe;

    public static CommentResponseDTO from(CommentEntity comment) {
        return from(comment, 0L, false);
    }

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
