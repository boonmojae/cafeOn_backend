package com.b1a4.cafeOn.chat.exception;

import com.b1a4.cafeOn.common.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class ChatRoomNotFoundException extends ClientErrorException {
    public ChatRoomNotFoundException(Long roomId) {
        super(HttpStatus.NOT_FOUND, "해당 채팅방을 찾을 수 없습니다. ID: " + roomId);
    }
}
