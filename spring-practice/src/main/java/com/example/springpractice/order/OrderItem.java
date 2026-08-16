package com.example.springpractice.order;

import com.example.springpractice.product.Product;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private int quantity;

    /** 주문 시점의 상품 가격 스냅샷. 이후 상품 가격이 바뀌어도 주문 내역은 보존된다. */
    private int orderPrice;

    private OrderItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.orderPrice = product.getPrice();
    }

    /** 주문 항목을 생성하면서 해당 상품의 재고를 차감한다. */
    public static OrderItem create(Product product, int quantity) {
        product.removeStock(quantity);
        return new OrderItem(product, quantity);
    }

    /** 연관관계 편의 메서드에서 호출. */
    void assignOrder(Order order) {
        this.order = order;
    }

    /** 이 항목의 주문 금액 (가격 스냅샷 × 수량). */
    public int getSubtotal() {
        return orderPrice * quantity;
    }
}
