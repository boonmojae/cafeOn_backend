package com.b1a4.cafeOn.image.repository;

import com.b1a4.cafeOn.community.post.entity.PostEntity;
import com.b1a4.cafeOn.image.entity.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, Long> {

    List<ImageEntity> findByPost(PostEntity post);
}
