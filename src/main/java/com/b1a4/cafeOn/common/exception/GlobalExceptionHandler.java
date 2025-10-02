package com.b1a4.cafeOn.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ClientErrorException 및 이를 상속하는 모든 예외 처리
    @ExceptionHandler(ClientErrorException.class)
    public ResponseEntity<ClientErrorResponseDTO> handleClientErrorException(ClientErrorException ex) {
        // ClientErrorException 내부에 정의된 HttpStatus와 메시지 사용
        return ResponseEntity.status(ex.getHttpStatus())
                .body(new ClientErrorResponseDTO(ex.getHttpStatus(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ClientErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.toList()); // List<String> 형태로 메시지를 수집

        // Object message 자리에 List<String>을 넘김으로써 유연하게 처리
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ClientErrorResponseDTO(HttpStatus.BAD_REQUEST, errors));
    }

    // JSON 파싱 오류 등 HTTP Body 읽기 실패 시 처리
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ClientErrorResponseDTO> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ClientErrorResponseDTO(HttpStatus.BAD_REQUEST, "요청 본문 형식이 올바르지 않습니다."));
    }

    // 처리되지 않은 모든 일반 예외 (가장 마지막에 처리)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ClientErrorResponseDTO> handleAllUncaughtException(Exception ex, HttpServletRequest request) {
        // 심각한 서버 오류이므로 500 Internal Server Error 반환
        LoggerFactory.getLogger(getClass())
                .error("Unhandled exception at {} {}",
                        request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ClientErrorResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."));
    }
}