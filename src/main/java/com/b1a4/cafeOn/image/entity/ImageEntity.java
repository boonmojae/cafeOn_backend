package com.b1a4.cafeOn.image.entity;

import com.b1a4.cafeOn.chat.entity.ChatEntity;
import com.b1a4.cafeOn.community.post.entity.PostEntity;
import com.b1a4.cafeOn.review.entity.ReviewEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "images",
        indexes = {
                @Index(name = "idx_images_post", columnList = "post_id"),
                @Index(name = "idx_images_review", columnList = "review_id"),
                @Index(name = "idx_images_chat", columnList = "chat_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @Column(name = "original_file_name", length = 255)
    private String originalFileName;

    @Column(name = "s3_key", length = 500, nullable = false)
    private String s3Key;

    @Column(name = "public_url", length = 1000)
    private String publicUrl;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = true)
    private PostEntity post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = true)
    private ReviewEntity review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = true)
    private ChatEntity chat;

    @PrePersist
    @PreUpdate
    private void validateOwner() {
        int count = 0;
        if (post != null) count++;
        if (review != null) count++;
        if (chat != null) count++;

        if (count != 1) {
            throw new IllegalStateException("ImageEntity는 post/review/chat 중 하나에만 연결되어야 합니다.");
        }
    }
}