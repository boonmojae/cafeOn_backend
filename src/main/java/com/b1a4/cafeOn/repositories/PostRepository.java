package com.b1a4.cafeOn.repositories;

import com.b1a4.cafeOn.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {

    Optional<PostEntity> findByPostIdAndUser_UserId(Long postId, String userId);
    Page<PostEntity> findAllByUser_UserId(String userId, Pageable pageable);
    Page<PostEntity> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String title, String content, Pageable pageable);
}
