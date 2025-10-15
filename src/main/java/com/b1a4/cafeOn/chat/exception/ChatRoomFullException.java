package com.b1a4.cafeOn.chat.exception;

import com.b1a4.cafeOn.common.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class ChatRoomFullException extends ClientErrorException {
    public ChatRoomFullException(Long cafeId, int limit) {
        super(HttpStatus.CONFLICT, "참여할 수 있는 채팅 인원이 마감되었습니다. id: " + cafeId + ", limit: " + limit);
    }
}
