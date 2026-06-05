package com.example.springpractice.order;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// "order" 는 SQL 예약어라 테이블명을 orders 로 지정한다.
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ordererName;

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    private Order(String ordererName) {
        this.ordererName = ordererName;
        this.orderedAt = LocalDateTime.now();
    }

    /**
     * 주문을 생성한다. 각 OrderItem 생성 시점에 상품 재고가 차감되며,
     * 도중에 재고가 부족하면 예외가 전파되어 트랜잭션 전체가 롤백된다.
     */
    public static Order create(String ordererName, List<OrderItem> orderItems) {
        Order order = new Order(ordererName);
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        return order;
    }

    /** 연관관계 편의 메서드: 양방향 참조를 함께 세팅한다. */
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.assignOrder(this);
    }

    /** 주문 총액. */
    public int getTotalAmount() {
        return orderItems.stream()
                .mapToInt(OrderItem::getSubtotal)
                .sum();
    }
}
