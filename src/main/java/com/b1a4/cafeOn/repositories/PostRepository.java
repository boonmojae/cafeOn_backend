package com.b1a4.cafeOn.repositories;

import com.b1a4.cafeOn.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {

    Optional<PostEntity> findByPostIdAndUserId(Long postId, String userId);
    List<PostEntity> findAllByUserId(String userId);
}
