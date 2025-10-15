package com.b1a4.cafeOn.chat.exception;

import com.b1a4.cafeOn.common.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class AlreadyInChatRoomException extends ClientErrorException {
    public AlreadyInChatRoomException() {
        super(HttpStatus.CONFLICT, "이미 채팅방에 참여 중입니다.");
    }
}
