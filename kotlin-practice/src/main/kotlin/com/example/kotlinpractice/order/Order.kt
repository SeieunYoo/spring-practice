package com.example.kotlinpractice.order

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDateTime

// "order" 는 SQL 예약어라 테이블명을 orders 로 지정한다.
@Entity
@Table(name = "orders")
class Order private constructor(
    @Column(nullable = false)
    var ordererName: String
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    @Column(nullable = false)
    var orderedAt: LocalDateTime = LocalDateTime.now()
        protected set

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    var orderItems: MutableList<OrderItem> = mutableListOf()
        protected set

    /** 연관관계 편의 메서드: 양방향 참조를 함께 세팅한다. */
    fun addOrderItem(orderItem: OrderItem) {
        orderItems.add(orderItem)
        orderItem.assignOrder(this)
    }

    /** 주문 총액. */
    val totalAmount: Int
        get() = orderItems.sumOf { it.subtotal }

    companion object {
        /**
         * 주문을 생성한다. 각 OrderItem 생성 시점에 상품 재고가 차감되며,
         * 도중에 재고가 부족하면 예외가 전파되어 트랜잭션 전체가 롤백된다.
         */
        fun create(ordererName: String, orderItems: List<OrderItem>): Order {
            val order = Order(ordererName)
            orderItems.forEach { order.addOrderItem(it) }
            return order
        }
    }
}
