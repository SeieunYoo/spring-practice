package com.example.springpractice.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.springpractice.common.exception.InsufficientStockException;
import com.example.springpractice.common.exception.NotFoundException;
import com.example.springpractice.order.dto.OrderCreateRequest;
import com.example.springpractice.order.dto.OrderCreateRequest.OrderItemRequest;
import com.example.springpractice.order.dto.OrderResponse;
import com.example.springpractice.product.Product;
import com.example.springpractice.product.ProductRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// 롤백을 DB 수준에서 검증하기 위해 클래스에 @Transactional 을 두지 않는다.
// (테스트가 트랜잭션을 잡고 있으면 같은 영속성 컨텍스트라 롤백 여부를 확인할 수 없다)
@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
    }

    private Product saveProduct(String name, int price, int stock) {
        return productRepository.save(
                Product.builder().name(name).price(price).stockQuantity(stock).build());
    }

    @Test
    @DisplayName("정상 주문 시 주문이 저장되고 상품 재고가 차감된다")
    void create_success() {
        Product product = saveProduct("티셔츠", 19000, 10);

        OrderResponse response = orderService.create(new OrderCreateRequest(
                "유세은", List.of(new OrderItemRequest(product.getId(), 3))));

        assertThat(response.orderId()).isNotNull();
        assertThat(response.totalAmount()).isEqualTo(19000 * 3);
        // DB 에서 다시 읽어 재고 차감 확인
        assertThat(productRepository.findById(product.getId()).orElseThrow()
                .getStockQuantity()).isEqualTo(7);
    }

    @Test
    @DisplayName("재고보다 많이 주문하면 InsufficientStockException 이 발생한다")
    void create_insufficientStock() {
        Product product = saveProduct("티셔츠", 19000, 2);

        assertThatThrownBy(() -> orderService.create(new OrderCreateRequest(
                "유세은", List.of(new OrderItemRequest(product.getId(), 5)))))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    @DisplayName("여러 상품 중 하나라도 재고가 부족하면 전체가 롤백된다 (앞 상품 재고도 차감되지 않음)")
    void create_rollback() {
        Product enough = saveProduct("티셔츠", 19000, 10);   // 충분
        Product scarce = saveProduct("청바지", 49000, 1);    // 부족

        assertThatThrownBy(() -> orderService.create(new OrderCreateRequest(
                "유세은", List.of(
                        new OrderItemRequest(enough.getId(), 2),   // 먼저 차감 시도
                        new OrderItemRequest(scarce.getId(), 5))))) // 여기서 실패
                .isInstanceOf(InsufficientStockException.class);

        // 트랜잭션 롤백으로 앞 상품 재고도 원래대로, 주문도 저장되지 않음
        assertThat(productRepository.findById(enough.getId()).orElseThrow()
                .getStockQuantity()).isEqualTo(10);
        assertThat(orderRepository.count()).isZero();
    }

    @Test
    @DisplayName("존재하지 않는 상품을 주문하면 NotFoundException 이 발생한다")
    void create_productNotFound() {
        assertThatThrownBy(() -> orderService.create(new OrderCreateRequest(
                "유세은", List.of(new OrderItemRequest(999L, 1)))))
                .isInstanceOf(NotFoundException.class);
    }
}
