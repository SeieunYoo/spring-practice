package com.example.kotlinpractice.order

import com.example.kotlinpractice.common.exception.NotFoundException
import com.example.kotlinpractice.order.dto.OrderCreateRequest
import com.example.kotlinpractice.order.dto.OrderCreateRequest.OrderItemRequest
import com.example.kotlinpractice.order.dto.OrderResponse
import com.example.kotlinpractice.product.Product
import com.example.kotlinpractice.product.ProductRepository
import jakarta.persistence.EntityManagerFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.hibernate.SessionFactory
import org.hibernate.stat.Statistics
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

// generate_statistics 로 실제 실행된 쿼리 수를 세어 N+1 회피(단일 쿼리)를 검증한다.
@SpringBootTest(properties = ["spring.jpa.properties.hibernate.generate_statistics=true"])
class OrderQueryTest {

    @Autowired
    private lateinit var orderService: OrderService

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var emf: EntityManagerFactory

    @AfterEach
    fun tearDown() {
        orderRepository.deleteAll()
        productRepository.deleteAll()
    }

    private fun statistics(): Statistics = emf.unwrap(SessionFactory::class.java).statistics

    private fun saveProduct(name: String, price: Int, stock: Int): Product =
        productRepository.save(Product.create(name, price, stock))

    private fun order(orderer: String, product: Product, quantity: Int): OrderResponse =
        orderService.create(
            OrderCreateRequest(orderer, listOf(OrderItemRequest(product.id, quantity)))
        )

    @Test
    @DisplayName("주문 목록을 fetch join 으로 조회하면 항목/상품 접근에도 추가 쿼리가 없다 (N+1 회피)")
    fun findAll_singleQuery() {
        val tshirt = saveProduct("티셔츠", 19000, 100)
        val jeans = saveProduct("청바지", 49000, 100)
        order("유세은", tshirt, 2)
        order("김라포", jeans, 1)

        val statistics = statistics()
        statistics.clear()

        val result = orderService.findAll()

        // 결과 정합성 (DTO 변환 과정에서 orderItems, product 까지 모두 접근)
        assertThat(result).hasSize(2)
        assertThat(result.flatMap { it.items }.map { it.productName })
            .containsExactlyInAnyOrder("티셔츠", "청바지")
        // fetch join 덕분에 단 한 번의 쿼리
        assertThat(statistics.prepareStatementCount).isEqualTo(1)
    }

    @Test
    @DisplayName("주문 상세를 조회하면 항목과 상품 정보가 함께 조회된다")
    fun findById() {
        val tshirt = saveProduct("티셔츠", 19000, 100)
        val created = order("유세은", tshirt, 3)

        val found = orderService.findById(created.orderId!!)

        assertThat(found.ordererName).isEqualTo("유세은")
        assertThat(found.items).hasSize(1)
        assertThat(found.items[0].productName).isEqualTo("티셔츠")
        assertThat(found.totalAmount).isEqualTo(19000 * 3)
    }

    @Test
    @DisplayName("존재하지 않는 주문을 조회하면 NotFoundException 이 발생한다")
    fun findById_notFound() {
        assertThatThrownBy { orderService.findById(999L) }
            .isInstanceOf(NotFoundException::class.java)
    }

    @Test
    @DisplayName("상품별 주문 내역은 해당 상품이 포함된 주문만 반환한다")
    fun findByProductId() {
        val tshirt = saveProduct("티셔츠", 19000, 100)
        val jeans = saveProduct("청바지", 49000, 100)
        order("유세은", tshirt, 2)   // 티셔츠 주문
        order("김라포", jeans, 1)    // 청바지 주문

        val tshirtOrders = orderService.findByProductId(tshirt.id!!)

        assertThat(tshirtOrders).hasSize(1)
        assertThat(tshirtOrders[0].ordererName).isEqualTo("유세은")
    }

    @Test
    @DisplayName("존재하지 않는 상품의 주문 내역을 조회하면 NotFoundException 이 발생한다")
    fun findByProductId_productNotFound() {
        assertThatThrownBy { orderService.findByProductId(999L) }
            .isInstanceOf(NotFoundException::class.java)
    }
}
