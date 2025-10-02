package com.b1a4.cafeOn.common.exception;

import org.springframework.http.HttpStatus;

public class ClientErrorException extends RuntimeException{
    private final HttpStatus httpStatus;

    public ClientErrorException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
