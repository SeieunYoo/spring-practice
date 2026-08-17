package com.example.kotlinpractice.product.dto

import com.example.kotlinpractice.product.Product
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero

data class ProductCreateRequest(
    @field:NotBlank(message = "상품명은 필수입니다.")
    val name: String,

    @field:PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
    val price: Int,

    @field:PositiveOrZero(message = "재고 수량은 0 이상이어야 합니다.")
    val stockQuantity: Int
) {
    fun toEntity(): Product = Product.create(name, price, stockQuantity)
}
