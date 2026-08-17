package com.example.kotlinpractice.order.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class OrderCreateRequest(
    @field:NotBlank(message = "주문자 이름은 필수입니다.")
    val ordererName: String,

    @field:NotEmpty(message = "주문 항목은 1개 이상이어야 합니다.")
    @field:Valid
    val items: List<OrderItemRequest>
) {
    data class OrderItemRequest(
        @field:NotNull(message = "상품 id는 필수입니다.")
        val productId: Long?,

        @field:Positive(message = "주문 수량은 1 이상이어야 합니다.")
        val quantity: Int
    )
}
