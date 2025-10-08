package com.b1a4.cafeOn.community.comment.exception;

import com.b1a4.cafeOn.common.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class CommentNotFoundException extends ClientErrorException {
    public CommentNotFoundException(Long commentId) {
        super(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다. ID: " + commentId);
    }
}
