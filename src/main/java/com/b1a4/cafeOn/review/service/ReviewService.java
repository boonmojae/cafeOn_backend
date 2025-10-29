package com.b1a4.cafeOn.review.service;

import com.b1a4.cafeOn.cafe.entity.CafeEntity;
import com.b1a4.cafeOn.cafe.repository.CafeRepository;
import com.b1a4.cafeOn.common.exception.ReviewRatingMinMaxException;
import com.b1a4.cafeOn.image.entity.ImageEntity;
import com.b1a4.cafeOn.image.service.ImageService;
import com.b1a4.cafeOn.image.service.S3Service;
import com.b1a4.cafeOn.review.dto.ReviewRequestDTO;
import com.b1a4.cafeOn.review.dto.ReviewResponseDTO;
import com.b1a4.cafeOn.review.dto.ReviewUpdateRequestDTO;
import com.b1a4.cafeOn.review.entity.ReviewEntity;
import com.b1a4.cafeOn.review.exception.ReviewNotFoundException;
import com.b1a4.cafeOn.review.repository.ReviewRepository;
import com.b1a4.cafeOn.user.entity.UserEntity;
import com.b1a4.cafeOn.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final CafeRepository cafeRepository;
    private final ImageService imageService;

    // 리뷰 생성(글, 글+이미지)
    @Transactional
    public ReviewResponseDTO createReview(String userId, Long cafeId,
            ReviewRequestDTO reviewDTO, List<S3Service.UploadedImageInfo> uploadedImages) {

        UserEntity user = findByUserId(userId);
        CafeEntity cafe = findByCafeId(cafeId);

        if (reviewDTO == null) {
            throw new IllegalArgumentException("리뷰 정보가 필요합니다.");
        }
        if (reviewDTO.getContent() == null || reviewDTO.getContent().isBlank()) {
            throw new IllegalArgumentException("리뷰 내용은 필수입니다.");
        }
        validateRating(reviewDTO.getRating());

        ReviewEntity review = ReviewEntity.builder()
                .rating(reviewDTO.getRating())
                .content(reviewDTO.getContent())
                .user(user)
                .cafe(cafe)
                .build();

        ReviewEntity savedReview = reviewRepository.save(review);

        if (uploadedImages != null && !uploadedImages.isEmpty()) {
            for (S3Service.UploadedImageInfo imageInfo : uploadedImages) {
                ImageEntity imageEntity = imageService.attachNewImageToReview(savedReview, imageInfo);
                savedReview.addImage(imageEntity);
            }
        }

        return ReviewResponseDTO.fromEntity(savedReview);
    }


    // 리뷰 수정
    @Transactional
    public ReviewResponseDTO updateReview(String userId, Long reviewId,
            ReviewUpdateRequestDTO reviewDTO, List<S3Service.UploadedImageInfo> newlyUploadedImages) {

        UserEntity user = findByUserId(userId);

        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        boolean isOwner = review.getUser() != null
                && review.getUser().getUserId().equals(user.getUserId());
        boolean isAdmin = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!(isOwner || isAdmin)) {
            throw new AccessDeniedException("리뷰를 수정할 권한이 없습니다.");
        }

        if (reviewDTO == null) {
            throw new IllegalArgumentException("리뷰 정보가 필요합니다.");
        }
        if (reviewDTO.getContent() == null || reviewDTO.getContent().isBlank()) {
            throw new IllegalArgumentException("리뷰 내용은 필수입니다.");
        }

        validateRating(reviewDTO.getRating());

        review.update(reviewDTO.getRating(), reviewDTO.getContent());

        List<Long> imagesToKeepIds =
                (reviewDTO.getExistingImageIds() != null)
                        ? reviewDTO.getExistingImageIds()
                        : Collections.emptyList();

        imageService.updateReviewImages(
                review,
                imagesToKeepIds,
                newlyUploadedImages != null ? newlyUploadedImages : Collections.emptyList()
        );

        return ReviewResponseDTO.fromEntity(review);
    }


    // 리뷰 삭제
    @Transactional
    public void deleteReview(String userId, Long reviewId) {
        UserEntity user = findByUserId(userId);

        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        if (!isOwnerOrAdmin(user.getUserId(), review)) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        imageService.removeAllImagesOfReview(review);

        reviewRepository.delete(review);
    }


    // 내가 작성한 리뷰 목록 조회
    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> getReviewById(String userId, Pageable pageable) {

        Page<ReviewEntity> reviews = reviewRepository.findByUser_UserId(userId, pageable);

        return reviews.map(ReviewResponseDTO::fromEntity);
    }



    public UserEntity findByUserId(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. ID: " + userId));
    }

    public CafeEntity findByCafeId(Long cafeId) {
        return cafeRepository.findById(cafeId)
                .orElseThrow(() ->
                        new IllegalArgumentException("해당 카페를 찾을 수 없습니다. ID: " + cafeId));
    }

    private boolean isOwnerOrAdmin(String userId, ReviewEntity review) {
        boolean isOwner = review.getUser() != null
                && review.getUser().getUserId().equals(userId);

        boolean isAdmin = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return isOwner || isAdmin;
    }

    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new ReviewRatingMinMaxException();
        }
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsByCafeId(Long cafeId) {    // 단순조회+로그 메서드 추가
        List<ReviewEntity> reviews = reviewRepository.findByCafe_CafeId(cafeId);
        log.info("🔎 [ReviewService] cafeId={} -> reviews found: {}", cafeId, reviews.size());

        // 리뷰 각각 id도 찍어보기
        reviews.forEach(r -> log.info("   - reviewId={}, rating={}, userId={}, images={}",
                r.getReviewId(), r.getRating(),
                r.getUser() != null ? r.getUser().getUserId() : "null",
                r.getImages() != null ? r.getImages().size() : -1));
        return ReviewResponseDTO.fromEntities(reviews);
    }
}