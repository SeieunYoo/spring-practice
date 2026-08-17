package com.example.kotlinpractice.common.exception

/** 요청한 리소스를 찾을 수 없을 때 발생. GlobalExceptionHandler 에서 404 로 변환된다. */
class NotFoundException(message: String) : RuntimeException(message)
