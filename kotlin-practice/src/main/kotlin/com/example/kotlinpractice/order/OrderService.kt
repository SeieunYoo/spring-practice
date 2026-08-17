package com.example.kotlinpractice.order

import com.example.kotlinpractice.common.exception.NotFoundException
import com.example.kotlinpractice.order.dto.OrderCreateRequest
import com.example.kotlinpractice.order.dto.OrderResponse
import com.example.kotlinpractice.product.Product
import com.example.kotlinpractice.product.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class OrderService(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository
) {

    /**
     * 주문을 생성한다.
     * 주문 저장과 재고 차감이 하나의 트랜잭션으로 묶여 있어,
     * 항목 중 하나라도 재고가 부족하면 앞서 차감된 재고까지 전부 롤백된다.
     */
    @Transactional
    fun create(request: OrderCreateRequest): OrderResponse {
        val orderItems = request.items.map { item ->
            val product = getProductOrThrow(item.productId!!)
            OrderItem.create(product, item.quantity) // 여기서 재고 차감
        }

        val order = Order.create(request.ordererName, orderItems)
        val saved = orderRepository.save(order) // cascade 로 OrderItem 함께 저장
        return OrderResponse.from(saved)
    }

    /** 주문 목록 조회. fetch join 으로 주문+항목+상품을 한 번에 로딩(N+1 회피). */
    fun findAll(): List<OrderResponse> =
        orderRepository.findAllWithItems().map { OrderResponse.from(it) }

    /** 주문 단건 상세 조회. */
    fun findById(id: Long): OrderResponse {
        val order = orderRepository.findByIdWithItems(id)
            ?: throw NotFoundException("주문을 찾을 수 없습니다. id=$id")
        return OrderResponse.from(order)
    }

    /** 특정 상품이 포함된 주문 내역 조회. 상품이 없으면 404. */
    fun findByProductId(productId: Long): List<OrderResponse> {
        if (!productRepository.existsById(productId)) {
            throw NotFoundException("상품을 찾을 수 없습니다. id=$productId")
        }
        return orderRepository.findAllByProductId(productId).map { OrderResponse.from(it) }
    }

    private fun getProductOrThrow(id: Long): Product =
        productRepository.findById(id)
            .orElseThrow { NotFoundException("상품을 찾을 수 없습니다. id=$id") }
}
