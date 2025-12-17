package com.b1a4.cafeOn.cafe.exception;

import com.b1a4.cafeOn.common.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class KakaoApiRequestException extends ClientErrorException {
    public KakaoApiRequestException() {
        super(HttpStatus.BAD_REQUEST, "카카오 API 요청에 필요한 정보가 잘못되었습니다.");
    }
}
