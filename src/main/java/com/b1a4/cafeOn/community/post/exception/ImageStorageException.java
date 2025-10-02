package com.b1a4.cafeOn.community.post.exception;

import com.b1a4.cafeOn.common.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class ImageStorageException extends ClientErrorException {
    public ImageStorageException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
