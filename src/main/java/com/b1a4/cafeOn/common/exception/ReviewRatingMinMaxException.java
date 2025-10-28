package com.b1a4.cafeOn.common.exception;

import org.springframework.http.HttpStatus;

public class ReviewRatingMinMaxException extends ClientErrorException{
    public ReviewRatingMinMaxException() {
        super(HttpStatus.BAD_REQUEST, "별점은 1~5 사이로만 입력할 수 있습니다.");
    }
}
