package com.b1a4.cafeOn.review.exception;

import com.b1a4.cafeOn.common.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class CafeReviewNotFoundException extends ClientErrorException {
    public CafeReviewNotFoundException(Long cafeId, Long reviewId) {
        super(HttpStatus.NOT_FOUND, "해당 카페의 리뷰가 아닙니다. cafeId:" + cafeId + ", reviewId:" + reviewId);
    }
}
