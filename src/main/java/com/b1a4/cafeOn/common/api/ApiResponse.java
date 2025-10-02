package com.b1a4.cafeOn.common.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
// UserDTO를 응답에 그대로 쓰면 message를 못 담으니, message + 데이터 조합을 사용할 수 있도록 만듬
public class ApiResponse<T> {
    private String message;
    private T data; // UserDTO, PostDTO .. 등등을 다 얘로 넣어서 처리해버림
}