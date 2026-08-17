package com.example.kotlinpractice.order.dto

import com.example.kotlinpractice.order.Order
import com.example.kotlinpractice.order.OrderItem
import java.time.LocalDateTime

data class OrderResponse(
    val orderId: Long?,
    val ordererName: String,
    val orderedAt: LocalDateTime,
    val items: List<OrderItemResponse>,
    val totalAmount: Int
) {
    data class OrderItemResponse(
        val productId: Long?,
        val productName: String,
        val quantity: Int,
        val orderPrice: Int,
        val subtotal: Int
    ) {
        companion object {
            fun from(orderItem: OrderItem): OrderItemResponse =
                OrderItemResponse(
                    orderItem.product.id,
                    orderItem.product.name,
                    orderItem.quantity,
                    orderItem.orderPrice,
                    orderItem.subtotal
                )
        }
    }

    companion object {
        fun from(order: Order): OrderResponse {
            val items = order.orderItems.map { OrderItemResponse.from(it) }
            return OrderResponse(
                order.id,
                order.ordererName,
                order.orderedAt,
                items,
                order.totalAmount
            )
        }
    }
}
