package com.example.kotlinpractice.order

import com.example.kotlinpractice.common.exception.InsufficientStockException
import com.example.kotlinpractice.common.exception.NotFoundException
import com.example.kotlinpractice.order.dto.OrderCreateRequest
import com.example.kotlinpractice.order.dto.OrderCreateRequest.OrderItemRequest
import com.example.kotlinpractice.product.Product
import com.example.kotlinpractice.product.ProductRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

// 롤백을 DB 수준에서 검증하기 위해 클래스에 @Transactional 을 두지 않는다.
// (테스트가 트랜잭션을 잡고 있으면 같은 영속성 컨텍스트라 롤백 여부를 확인할 수 없다)
@SpringBootTest
class OrderServiceTest {

    @Autowired
    private lateinit var orderService: OrderService

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @AfterEach
    fun tearDown() {
        orderRepository.deleteAll()
        productRepository.deleteAll()
    }

    private fun saveProduct(name: String, price: Int, stock: Int): Product =
        productRepository.save(Product.create(name, price, stock))

    @Test
    @DisplayName("정상 주문 시 주문이 저장되고 상품 재고가 차감된다")
    fun create_success() {
        val product = saveProduct("티셔츠", 19000, 10)

        val response = orderService.create(
            OrderCreateRequest("유세은", listOf(OrderItemRequest(product.id, 3)))
        )

        assertThat(response.orderId).isNotNull()
        assertThat(response.totalAmount).isEqualTo(19000 * 3)
        // DB 에서 다시 읽어 재고 차감 확인
        assertThat(productRepository.findById(product.id!!).orElseThrow().stockQuantity)
            .isEqualTo(7)
    }

    @Test
    @DisplayName("재고보다 많이 주문하면 InsufficientStockException 이 발생한다")
    fun create_insufficientStock() {
        val product = saveProduct("티셔츠", 19000, 2)

        assertThatThrownBy {
            orderService.create(
                OrderCreateRequest("유세은", listOf(OrderItemRequest(product.id, 5)))
            )
        }.isInstanceOf(InsufficientStockException::class.java)
    }

    @Test
    @DisplayName("여러 상품 중 하나라도 재고가 부족하면 전체가 롤백된다 (앞 상품 재고도 차감되지 않음)")
    fun create_rollback() {
        val enough = saveProduct("티셔츠", 19000, 10)   // 충분
        val scarce = saveProduct("청바지", 49000, 1)    // 부족

        assertThatThrownBy {
            orderService.create(
                OrderCreateRequest(
                    "유세은",
                    listOf(
                        OrderItemRequest(enough.id, 2),   // 먼저 차감 시도
                        OrderItemRequest(scarce.id, 5)    // 여기서 실패
                    )
                )
            )
        }.isInstanceOf(InsufficientStockException::class.java)

        // 트랜잭션 롤백으로 앞 상품 재고도 원래대로, 주문도 저장되지 않음
        assertThat(productRepository.findById(enough.id!!).orElseThrow().stockQuantity)
            .isEqualTo(10)
        assertThat(orderRepository.count()).isZero()
    }

    @Test
    @DisplayName("존재하지 않는 상품을 주문하면 NotFoundException 이 발생한다")
    fun create_productNotFound() {
        assertThatThrownBy {
            orderService.create(
                OrderCreateRequest("유세은", listOf(OrderItemRequest(999L, 1)))
            )
        }.isInstanceOf(NotFoundException::class.java)
    }
}
