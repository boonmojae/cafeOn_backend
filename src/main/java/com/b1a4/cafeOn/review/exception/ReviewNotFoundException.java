package com.b1a4.cafeOn.review.exception;

import com.b1a4.cafeOn.common.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class ReviewNotFoundException extends ClientErrorException {
    public ReviewNotFoundException(Long reviewId) {
        super(HttpStatus.NOT_FOUND, "해당 리뷰를 찾을 수 없습니다. ID: " + reviewId);
    }
}