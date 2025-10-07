package com.b1a4.cafeOn.community.post.dto;

import com.b1a4.cafeOn.community.comment.dto.CommentResponseDTO;
import com.b1a4.cafeOn.image.dto.ImageResponseDTO;
import com.b1a4.cafeOn.community.post.entity.PostEntity;
import com.b1a4.cafeOn.community.post.enums.PostType;
import com.b1a4.cafeOn.user.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostDetailResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String authorNickname;
    private PostType type;
    private long viewCount;
    private List<ImageResponseDTO> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long likeCount;
    private boolean likedByMe;

    // 게시글 상세 조회(댓글 데이터 X, 댓글 API사용)
    public static PostDetailResponseDTO from(PostEntity post, long likeCount, boolean likedByMe) {

        // 탈퇴한 회원 닉네임
        String nicknameToDisplay;

        if (post.getUser() == null || post.getUser().getStatus() == UserStatus.DELETED) {
            nicknameToDisplay = "(빈 자리)";
        } else {
            nicknameToDisplay = post.getUser().getNickname();
        }

        List<ImageResponseDTO> imageResponseDTOS = post.getImages().stream()
                .map(ImageResponseDTO::from)
                .collect(Collectors.toList());

        return PostDetailResponseDTO.builder()
                .id(post.getPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorNickname(nicknameToDisplay)
                .type(post.getType())
                .images(imageResponseDTOS)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .viewCount(post.getViewCount())
                .likeCount(likeCount)
                .likedByMe(likedByMe)
                .build();
    }
}

