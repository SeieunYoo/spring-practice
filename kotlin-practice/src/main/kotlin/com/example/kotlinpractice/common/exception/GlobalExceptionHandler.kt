package com.example.kotlinpractice.common.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(e: NotFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.of(HttpStatus.NOT_FOUND.value(), e.message ?: "찾을 수 없습니다."))

    @ExceptionHandler(InsufficientStockException::class)
    fun handleInsufficientStock(e: InsufficientStockException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse.of(HttpStatus.CONFLICT.value(), e.message ?: "재고가 부족합니다."))

    /** @Valid 검증 실패 시 첫 번째 필드 에러 메시지를 400 으로 반환. */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = e.bindingResult.fieldErrors
            .firstOrNull()
            ?.defaultMessage
            ?: "잘못된 요청입니다."
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), message))
    }
}
