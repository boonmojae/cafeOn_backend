package com.b1a4.cafeOn.chat.exception;

import com.b1a4.cafeOn.common.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class NotChatRoomMemberException extends ClientErrorException {
    public NotChatRoomMemberException() {
        super(HttpStatus.FORBIDDEN, "채팅방 멤버가 아닙니다.");
    }
}
