package com.b1a4.cafeOn.image.repository;

import com.b1a4.cafeOn.chat.entity.ChatEntity;
import com.b1a4.cafeOn.community.post.entity.PostEntity;
import com.b1a4.cafeOn.image.entity.ImageEntity;
import com.b1a4.cafeOn.review.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, Long> {

    // 게시글
    List<ImageEntity> findByPost(PostEntity post);

    // 리뷰
    List<ImageEntity> findByReview(ReviewEntity review);

    // 채팅
    List<ImageEntity> findByChat(ChatEntity chat);

    // 채팅방 모든 이미지
    @Query("""
        select i.s3Key
          from ImageEntity i
          join i.chat c
         where c.chatRoom.chatRoomId = :roomId
    """)
    List<String> findAllS3KeysByRoomId(@Param("roomId") Long roomId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        delete from ImageEntity i
         where i.chat.chatRoom.chatRoomId = :roomId
    """)
    int bulkDeleteByRoomId(@Param("roomId") Long roomId);

}
