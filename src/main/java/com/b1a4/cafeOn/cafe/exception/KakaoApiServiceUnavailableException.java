package com.b1a4.cafeOn.cafe.exception;

import com.b1a4.cafeOn.common.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class KakaoApiServiceUnavailableException extends ClientErrorException {
    public KakaoApiServiceUnavailableException() {
        super(HttpStatus.SERVICE_UNAVAILABLE, "카카오 API 서버에 일시적인 문제가 발생했습니다.");
    }
}
