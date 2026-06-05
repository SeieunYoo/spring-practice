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

    private Product getProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("상품을 찾을 수 없습니다. id=" + id));
    }
}
