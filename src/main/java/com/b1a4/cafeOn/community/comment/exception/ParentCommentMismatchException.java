package com.b1a4.cafeOn.community.comment.exception;

import com.b1a4.cafeOn.common.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class ParentCommentMismatchException extends ClientErrorException {
    public ParentCommentMismatchException(Long commentId, Long postId) {
        super(HttpStatus.NOT_FOUND, "댓글 " + commentId + " 은(는) 게시글 " + postId + " 에 속하지 않습니다.");
    }
}
