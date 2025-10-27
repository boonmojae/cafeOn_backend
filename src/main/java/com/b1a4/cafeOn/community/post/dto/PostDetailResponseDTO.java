package com.b1a4.cafeOn.community.post.dto;

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
    private String authorProfileImageUrl;
    private PostType type;
    private long viewCount;
    private List<ImageResponseDTO> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long likeCount;
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
