package com.b1a4.cafeOn.cafe.exception;

import com.b1a4.cafeOn.common.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class KakaoApiConnectionException extends ClientErrorException {
    public KakaoApiConnectionException() {
        super(HttpStatus.SERVICE_UNAVAILABLE, "외부 카카오 API에 연결할 수 없습니다.");
    }
}
