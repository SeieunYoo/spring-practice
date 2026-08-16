package com.example.springpractice.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.springpractice.common.exception.NotFoundException;
import com.example.springpractice.order.dto.OrderCreateRequest;
import com.example.springpractice.order.dto.OrderCreateRequest.OrderItemRequest;
import com.example.springpractice.order.dto.OrderResponse;
import com.example.springpractice.product.Product;
import com.example.springpractice.product.ProductRepository;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// generate_statistics 로 실제 실행된 쿼리 수를 세어 N+1 회피(단일 쿼리)를 검증한다.
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class OrderQueryTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManagerFactory emf;

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
    }

    private Statistics statistics() {
        return emf.unwrap(SessionFactory.class).getStatistics();
    }

    private Product saveProduct(String name, int price, int stock) {
        return productRepository.save(
                Product.builder().name(name).price(price).stockQuantity(stock).build());
    }

    private OrderResponse order(String orderer, Product product, int quantity) {
        return orderService.create(new OrderCreateRequest(
                orderer, List.of(new OrderItemRequest(product.getId(), quantity))));
    }

    @Test
    @DisplayName("주문 목록을 fetch join 으로 조회하면 항목/상품 접근에도 추가 쿼리가 없다 (N+1 회피)")
    void findAll_singleQuery() {
        Product tshirt = saveProduct("티셔츠", 19000, 100);
        Product jeans = saveProduct("청바지", 49000, 100);
        order("유세은", tshirt, 2);
        order("김라포", jeans, 1);

        Statistics statistics = statistics();
        statistics.clear();

        List<OrderResponse> result = orderService.findAll();

        // 결과 정합성 (DTO 변환 과정에서 orderItems, product 까지 모두 접근)
        assertThat(result).hasSize(2);
        assertThat(result).flatExtracting(OrderResponse::items)
                .extracting(i -> i.productName())
                .containsExactlyInAnyOrder("티셔츠", "청바지");
        // fetch join 덕분에 단 한 번의 쿼리
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("주문 상세를 조회하면 항목과 상품 정보가 함께 조회된다")
    void findById() {
        Product tshirt = saveProduct("티셔츠", 19000, 100);
        OrderResponse created = order("유세은", tshirt, 3);

        OrderResponse found = orderService.findById(created.orderId());

        assertThat(found.ordererName()).isEqualTo("유세은");
        assertThat(found.items()).hasSize(1);
        assertThat(found.items().get(0).productName()).isEqualTo("티셔츠");
        assertThat(found.totalAmount()).isEqualTo(19000 * 3);
    }

    @Test
    @DisplayName("존재하지 않는 주문을 조회하면 NotFoundException 이 발생한다")
    void findById_notFound() {
        assertThatThrownBy(() -> orderService.findById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("상품별 주문 내역은 해당 상품이 포함된 주문만 반환한다")
    void findByProductId() {
        Product tshirt = saveProduct("티셔츠", 19000, 100);
        Product jeans = saveProduct("청바지", 49000, 100);
        order("유세은", tshirt, 2);   // 티셔츠 주문
        order("김라포", jeans, 1);    // 청바지 주문

        List<OrderResponse> tshirtOrders = orderService.findByProductId(tshirt.getId());

        assertThat(tshirtOrders).hasSize(1);
        assertThat(tshirtOrders.get(0).ordererName()).isEqualTo("유세은");
    }

    @Test
    @DisplayName("존재하지 않는 상품의 주문 내역을 조회하면 NotFoundException 이 발생한다")
    void findByProductId_productNotFound() {
        assertThatThrownBy(() -> orderService.findByProductId(999L))
                .isInstanceOf(NotFoundException.class);
    }
}
