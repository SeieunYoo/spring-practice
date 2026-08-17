package com.example.kotlinpractice.product

import com.example.kotlinpractice.common.exception.InsufficientStockException
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Product private constructor(
    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var price: Int,

    @Column(nullable = false)
    var stockQuantity: Int
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    /** 상품 정보를 수정한다. */
    fun update(name: String, price: Int, stockQuantity: Int) {
        this.name = name
        this.price = price
        this.stockQuantity = stockQuantity
    }

    /** 주문 수량만큼 재고를 차감한다. 재고가 부족하면 예외를 던진다. */
    fun removeStock(quantity: Int) {
        val restStock = stockQuantity - quantity
        if (restStock < 0) {
            throw InsufficientStockException(
                "재고가 부족합니다. 상품=$name, 현재 재고=$stockQuantity, 요청 수량=$quantity"
            )
        }
        stockQuantity = restStock
    }

    companion object {
        fun create(name: String, price: Int, stockQuantity: Int): Product =
            Product(name, price, stockQuantity)
    }
}
