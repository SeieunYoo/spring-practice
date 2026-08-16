package com.example.springpractice.order;

import com.example.springpractice.common.exception.NotFoundException;
import com.example.springpractice.order.dto.OrderCreateRequest;
import com.example.springpractice.order.dto.OrderResponse;
import com.example.springpractice.product.Product;
import com.example.springpractice.product.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    /**
     * 주문을 생성한다.
     * 주문 저장과 재고 차감이 하나의 트랜잭션으로 묶여 있어,
     * 항목 중 하나라도 재고가 부족하면 앞서 차감된 재고까지 전부 롤백된다.
     */
    @Transactional
    public OrderResponse create(OrderCreateRequest request) {
        List<OrderItem> orderItems = request.items().stream()
                .map(item -> {
                    Product product = getProductOrThrow(item.productId());
                    return OrderItem.create(product, item.quantity()); // 여기서 재고 차감
                })
                .toList();

        Order order = Order.create(request.ordererName(), orderItems);
        Order saved = orderRepository.save(order); // cascade 로 OrderItem 함께 저장
        return OrderResponse.from(saved);
    }

    /** 주문 목록 조회. fetch join 으로 주문+항목+상품을 한 번에 로딩(N+1 회피). */
    public List<OrderResponse> findAll() {
        return orderRepository.findAllWithItems().stream()
                .map(OrderResponse::from)
                .toList();
    }

    /** 주문 단건 상세 조회. */
    public OrderResponse findById(Long id) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new NotFoundException("주문을 찾을 수 없습니다. id=" + id));
        return OrderResponse.from(order);
    }

    /** 특정 상품이 포함된 주문 내역 조회. 상품이 없으면 404. */
    public List<OrderResponse> findByProductId(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("상품을 찾을 수 없습니다. id=" + productId);
        }
        return orderRepository.findAllByProductId(productId).stream()
                .map(OrderResponse::from)
                .toList();
    }

    private Product getProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("상품을 찾을 수 없습니다. id=" + id));
    }
}
