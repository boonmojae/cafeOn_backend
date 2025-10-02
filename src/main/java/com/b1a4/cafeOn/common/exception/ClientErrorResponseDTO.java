package com.b1a4.cafeOn.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ClientErrorResponseDTO(HttpStatus httpStatus, Object message) {
}
