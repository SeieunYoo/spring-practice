package com.example.kotlinpractice.order

import com.example.kotlinpractice.product.Product
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class OrderItem private constructor(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product,

    var quantity: Int
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: Order? = null
        protected set

    /** 주문 시점의 상품 가격 스냅샷. 이후 상품 가격이 바뀌어도 주문 내역은 보존된다. */
    var orderPrice: Int = product.price
        protected set

    /** 연관관계 편의 메서드에서 호출. */
    internal fun assignOrder(order: Order) {
        this.order = order
    }

    /** 이 항목의 주문 금액 (가격 스냅샷 × 수량). */
    val subtotal: Int
        get() = orderPrice * quantity

    companion object {
        /** 주문 항목을 생성하면서 해당 상품의 재고를 차감한다. */
        fun create(product: Product, quantity: Int): OrderItem {
            product.removeStock(quantity)
            return OrderItem(product, quantity)
        }
    }
}
