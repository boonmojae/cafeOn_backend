package com.b1a4.cafeOn.dto.post;

import com.b1a4.cafeOn.entity.PostEntity;
import com.b1a4.cafeOn.enums.PostType;
import com.b1a4.cafeOn.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostDetailResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String authorNickname;
    private String imageUrl;
    private PostType type;
    private long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // fixme: CommentEntity 추가할때 주석 해제
    // private List<CommentDTO> comment;

    public static PostDetailResponseDTO from(PostEntity post) {

        // 탈퇴한 회원 닉네임
        String nicknameToDisplay;

        if (post.getUser() == null || post.getUser().getStatus() == UserStatus.DELETED) {
            nicknameToDisplay = "(빈 자리)";
        } else {
            nicknameToDisplay = post.getUser().getNickname();
        }

        return PostDetailResponseDTO.builder()
                .id(post.getPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorNickname(nicknameToDisplay)
                .imageUrl(post.getImageUrl())
                .type(post.getType())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .viewCount(post.getViewCount())
                .build();
    }
}

