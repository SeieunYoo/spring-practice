package com.example.springpractice.common.exception;

/** 요청한 리소스를 찾을 수 없을 때 발생. GlobalExceptionHandler 에서 404 로 변환된다. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
