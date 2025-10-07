package com.b1a4.cafeOn.community.post.dto;

import com.b1a4.cafeOn.community.post.entity.PostEntity;
import com.b1a4.cafeOn.community.post.enums.PostType;
import com.b1a4.cafeOn.user.enums.UserStatus;
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
    private long viewCount;
    private long likeCount;
    private long commentCount;
    private boolean likedByMe;

    // 게시글 전체 목록 + 댓글 카운트
    public static PostListResponseDTO from(PostEntity post, long likeCount, long commentCount, boolean likedByMe) {

        String nicknameToDisplay;

        if (post.getUser() == null || post.getUser().getStatus() == UserStatus.DELETED) {
            nicknameToDisplay = "(빈 자리)";
        } else {
            nicknameToDisplay = post.getUser().getNickname();
        }

        return PostListResponseDTO.builder()
                .id(post.getPostId())
                .type(post.getType())
                .title(post.getTitle())
                .authorNickname(nicknameToDisplay)
                .viewCount(post.getViewCount())
                .likeCount(likeCount)
                .likedByMe(likedByMe)
                .commentCount(commentCount)
                .createdAt(post.getCreatedAt())
                .build();
    }
}
