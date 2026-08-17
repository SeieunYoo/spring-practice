package com.example.kotlinpractice.common.exception

import java.time.LocalDateTime

data class ErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val message: String
) {
    companion object {
        fun of(status: Int, message: String): ErrorResponse =
            ErrorResponse(LocalDateTime.now(), status, message)
    }
}
