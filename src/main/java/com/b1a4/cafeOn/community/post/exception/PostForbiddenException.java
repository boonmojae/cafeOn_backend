package com.b1a4.cafeOn.community.post.exception;

import com.b1a4.cafeOn.common.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class PostForbiddenException extends ClientErrorException {
    public PostForbiddenException() {
        super(HttpStatus.FORBIDDEN, "이 게시글을 수정/삭제할 권한이 없습니다.");
    }
}
