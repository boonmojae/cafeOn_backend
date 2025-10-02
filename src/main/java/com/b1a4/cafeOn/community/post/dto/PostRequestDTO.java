package com.b1a4.cafeOn.community.post.dto;

import com.b1a4.cafeOn.community.post.enums.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostRequestDTO {
    
    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    private String title;

    @NotBlank(message = "내용은 필수 입력 항목입니다.")
    private String content;
    
    @NotNull(message = "게시글 타입을 선택해주세요")
    private PostType type;
    
    // 수정 시 유지할 기존 이미지들의 ID 리스트
    @Builder.Default
    private List<Long> existingImageIds = new ArrayList<>();

}
