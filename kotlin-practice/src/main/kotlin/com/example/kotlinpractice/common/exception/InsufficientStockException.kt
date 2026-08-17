package com.example.kotlinpractice.common.exception

/** 주문 수량이 재고보다 많을 때 발생. GlobalExceptionHandler 에서 409 로 변환된다. */
class InsufficientStockException(message: String) : RuntimeException(message)
