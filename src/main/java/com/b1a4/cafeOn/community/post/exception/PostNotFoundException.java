package com.b1a4.cafeOn.community.post.exception;

import com.b1a4.cafeOn.common.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class PostNotFoundException extends ClientErrorException {
    public PostNotFoundException(Long postId) {
        super(HttpStatus.NOT_FOUND, "해당 게시글을 찾을 수 없습니다. ID: " + postId);
    }
}
