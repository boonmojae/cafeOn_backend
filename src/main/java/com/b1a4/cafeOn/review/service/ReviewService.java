package com.b1a4.cafeOn.review.service;

import com.b1a4.cafeOn.cafe.entity.CafeEntity;
import com.b1a4.cafeOn.cafe.repository.CafeRepository;
import com.b1a4.cafeOn.common.exception.ReviewRatingMinMaxException;
import com.b1a4.cafeOn.review.dto.ReviewDTO;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final CafeRepository cafeRepository;

    // 리뷰 작성(JSON)
    @Transactional
    public ReviewDTO createReview(ReviewDTO reviewDTO, String userId, Long cafeId) {

        validateRating(reviewDTO.getRating());

        UserEntity user = findByUserId(userId);
        CafeEntity cafe = findByCafeId(cafeId);

        ReviewEntity saved = reviewRepository.save(ReviewDTO.toEntity(reviewDTO, user, cafe));

        return ReviewDTO.fromEntity(saved);

    }

    // todo
    // 리뷰 작성(JSON+멀티파트)

    // 리뷰 수정(JSON)
    @Transactional
    public ReviewDTO updateReview(ReviewDTO reviewDTO, String userId, Long reviewId) {

        validateRating(reviewDTO.getRating());

        UserEntity user = findByUserId(userId);

        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        assertOwner(user, review);

        review.update(reviewDTO.getRating(), reviewDTO.getContent());
        return ReviewDTO.fromEntity(review);
    }

    // todo
    // 리뷰 수정(JSON+멀티파트)

    // 리뷰 삭제
    @Transactional
    public void deleteReview(String userId, Long reviewId) {
        UserEntity user = findByUserId(userId);
        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        assertOwner(user, review);

        reviewRepository.delete(review);

    }


    // 특정 카페 전체 리뷰 조회
//    @Transactional(readOnly = true)
//    public Page<ReviewDTO> getAllReviewByCafeId(Long cafeId, Pageable pageable) {
//
//        Page<ReviewEntity> entities = reviewRepository.findByCafeIdWithJoins(cafeId, pageable);
//
//        return entities.map(ReviewDTO::fromEntity);
//    }


    // 특정 리뷰 조회(단건 조회가 아닌 목록기능으로 서비스 코드만 작성한 상태)
    @Transactional(readOnly = true)
    public ReviewDTO getReview(Long cafeId, Long reviewId) {
        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        if(!review.getCafe().getCafeId().equals(cafeId)) {
            throw new AccessDeniedException("해당 카페의 리뷰가 아닙니다.");
        }

        return ReviewDTO.fromEntity(review);
    }


    // 내가 작성한 리뷰 조회
    @Transactional(readOnly = true)
    public Page<ReviewDTO> getReviewById(String userId, Pageable pageable) {

        Page<ReviewEntity> entities = reviewRepository.findByUser_UserId(userId, pageable);

        return entities.map(ReviewDTO::fromEntity);
    }



    // 검증
    public UserEntity findByUserId(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. ID: " + userId));
    }

    public CafeEntity findByCafeId(Long cafeId) {
        return cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 카페를 찾을 수 없습니다. ID: " + cafeId));
    }

    private void assertOwner(UserEntity user, ReviewEntity review) {
        if (!review.getUser().getUserId().equals(user.getUserId())) {
            throw new AccessDeniedException("리뷰 작성자가 아닙니다.");
        }
    }

    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new ReviewRatingMinMaxException();
        }
    }


}
