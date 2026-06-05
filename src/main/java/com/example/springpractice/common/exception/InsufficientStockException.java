package com.example.springpractice.common.exception;

/** 주문 수량이 재고보다 많을 때 발생. GlobalExceptionHandler 에서 409 로 변환된다. */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }
}
