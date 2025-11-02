package com.b1a4.cafeOn.community.post.dto;

import com.b1a4.cafeOn.community.post.entity.PostEntity;
import com.b1a4.cafeOn.community.post.enums.PostType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(name = "PostListResponseDTO", description = "게시글 목록 아이템 DTO")
public class PostListResponseDTO {

    @Schema(description = "게시글 ID", example = "123")
    private Long id;

    @Schema(description = "게시글 유형", example = "FREE", allowableValues = {"FREE","NOTICE","QNA"})
    private PostType type;

    @Schema(description = "제목", example = "홍대 카페 추천합니다!")
    private String title;

    @Schema(description = "작성자 닉네임", example = "말차덕후")
    private String authorNickname;

    @Schema(description = "작성자 프로필 이미지 URL", example = "https://cdn.example.com/profiles/matcha.png", nullable = true)
    private String authorProfileImageUrl;

    @Schema(description = "작성 시각(ISO-8601)", example = "2025-11-02T12:34:56")
    private LocalDateTime createdAt;

    @Schema(description = "조회수", example = "257")
    private long viewCount;

    @Schema(description = "좋아요 수", example = "12")
    private long likeCount;

    @Schema(description = "댓글 수", example = "5")
    private long commentCount;

    @Schema(description = "내가 좋아요를 눌렀는지", example = "false")
    private boolean likedByMe;

    public static PostListResponseDTO from(PostEntity post, long likeCount, long commentCount, boolean likedByMe) {
        return PostListResponseDTO.builder()
                .id(post.getPostId())
                .type(post.getType())
                .title(post.getTitle())
                .authorNickname(post.getUser().getNickname())
                .authorProfileImageUrl(post.getUser().getProfileImage())
                .viewCount(post.getViewCount())
                .likeCount(likeCount)
                .likedByMe(likedByMe)
                .commentCount(commentCount)
                .createdAt(post.getCreatedAt())
                .build();
    }
}
