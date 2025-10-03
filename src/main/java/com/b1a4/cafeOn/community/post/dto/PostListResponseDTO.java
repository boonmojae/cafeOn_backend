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
    // fixme: CommentEntity 추가할때 주석 해제
    // private int commentCount;

    public static PostListResponseDTO from(PostEntity post, long likeCount) {

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
                .authorNickname(post.getUser().getNickname())
                .viewCount(post.getViewCount())
                .likeCount(likeCount)
                .createdAt(post.getCreatedAt())
                .build();
    }
}
