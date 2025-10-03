package com.b1a4.cafeOn.community.comment.dto;

import com.b1a4.cafeOn.community.comment.entity.CommentEntity;
import com.b1a4.cafeOn.community.post.entity.PostEntity;
import com.b1a4.cafeOn.user.entity.UserEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequestDTO {

    private Long parentId;

    @NotBlank(message = "내용을 작성해 주세요.")
    private String content;


    public static CommentEntity toEntity(PostEntity post, UserEntity user, CommentRequestDTO commentRequestDTO) {
        return CommentEntity.builder()
                .post(post)
                .user(user)
                .parentId(commentRequestDTO.getParentId())
                .content(commentRequestDTO.getContent())
                .build();
    }
}
