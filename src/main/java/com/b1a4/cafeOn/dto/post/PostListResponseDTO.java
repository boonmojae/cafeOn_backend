package com.b1a4.cafeOn.dto.post;

import com.b1a4.cafeOn.entity.PostEntity;
import com.b1a4.cafeOn.enums.PostType;
import com.b1a4.cafeOn.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.parameters.P;

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

    public static PostListResponseDTO from(PostEntity post) {

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
                .createdAt(post.getCreatedAt())
                .build();
    }
}
