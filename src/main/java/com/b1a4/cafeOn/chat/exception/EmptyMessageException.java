package com.b1a4.cafeOn.chat.exception;

import com.b1a4.cafeOn.common.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class EmptyMessageException extends ClientErrorException {
    public EmptyMessageException() {
        super(HttpStatus.BAD_REQUEST, "메시지를 입력해주세요.");
    }
}
