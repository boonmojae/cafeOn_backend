package com.b1a4.cafeOn.community.post.exception;

import com.b1a4.cafeOn.common.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

import java.awt.*;

public class ImageDeleteException extends ClientErrorException {
    public ImageDeleteException(String fileName, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 파일 삭제에 실패했습니다.: " + fileName);
    }
}
