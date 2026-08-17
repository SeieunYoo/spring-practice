package com.example.kotlinpractice.product.dto

import com.example.kotlinpractice.product.Product

data class ProductResponse(
    val id: Long?,
    val name: String,
    val price: Int,
    val stockQuantity: Int
) {
    companion object {
        fun from(product: Product): ProductResponse =
            ProductResponse(product.id, product.name, product.price, product.stockQuantity)
    }
}
