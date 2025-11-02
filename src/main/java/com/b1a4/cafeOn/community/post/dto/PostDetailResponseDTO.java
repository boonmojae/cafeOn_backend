package com.b1a4.cafeOn.community.post.dto;

import com.b1a4.cafeOn.image.dto.ImageResponseDTO;
import com.b1a4.cafeOn.community.post.entity.PostEntity;
import com.b1a4.cafeOn.community.post.enums.PostType;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "PostDetailResponseDTO", description = "게시글 상세 응답 DTO")
public class PostDetailResponseDTO {

    @Schema(description = "게시글 ID", example = "123")
    private Long id;

    @Schema(description = "제목", example = "홍대 카페 추천합니다!")
    private String title;

    @Schema(
            description = "내용",
            example = "몽블랑 빙수 진짜 맛있어요 😋 밤크림이 달지 않고 고소해서 커피랑 잘 어울려요. 주말 오후엔 조금 붐벼요!"
    )
    private String content;

    @Schema(description = "작성자 닉네임", example = "말차덕후")
    private String authorNickname;

    @Schema(description = "작성자 프로필 이미지 URL", example = "https://cdn.example.com/profiles/matcha.png", nullable = true)
    private String authorProfileImageUrl;

    @Schema(description = "게시글 유형", example = "FREE", allowableValues = {"FREE","NOTICE","QNA"})
    private PostType type;

    @Schema(description = "조회수", example = "257")
    private long viewCount;

    @Schema(description = "이미지 목록", nullable = true)
    private List<ImageResponseDTO> images;

    @Schema(description = "생성 시각(ISO-8601)", example = "2025-11-02T12:34:56")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시각(ISO-8601)", example = "2025-11-02T13:00:00", nullable = true)
    private LocalDateTime updatedAt;

    @Schema(description = "좋아요 수", example = "12")
    private long likeCount;

    @Schema(description = "내가 좋아요를 눌렀는지", example = "false")
    private boolean likedByMe;

    public static PostDetailResponseDTO from(PostEntity post, long likeCount, boolean likedByMe) {
        List<ImageResponseDTO> imageResponseDTOS = post.getImages().stream()
                .map(ImageResponseDTO::from)
                .collect(Collectors.toList());

        return PostDetailResponseDTO.builder()
                .id(post.getPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorNickname(post.getUser().getNickname())
                .authorProfileImageUrl(post.getUser().getProfileImage())
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
