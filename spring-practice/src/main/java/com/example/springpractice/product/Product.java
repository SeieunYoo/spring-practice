package com.example.springpractice.product;

import com.example.springpractice.common.exception.InsufficientStockException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int stockQuantity;

    /**
     * 낙관적 락용 버전 컬럼. 이 필드가 있으면 Hibernate가 모든 UPDATE에
     * "WHERE id=? AND version=?" 조건을 추가하고, 0건 갱신되면(=먼저 커밋된
     * 트랜잭션이 이미 버전을 올려놓은 상태) OptimisticLockException을 던진다.
     * 실제 주문 재고 차감 경로는 비관적 락({@link ProductRepository#findByIdForUpdate})을
     * 쓰므로 이 필드와 직접 충돌하지 않지만, 낙관적 락 동작을 별도로 관찰하기 위해 남겨둔다.
     */
    @Version
    private Long version;

    @Builder
    private Product(String name, int price, int stockQuantity) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    /** 상품 정보를 수정한다. */
    public void update(String name, int price, int stockQuantity) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    /** 주문 수량만큼 재고를 차감한다. 재고가 부족하면 예외를 던진다. */
    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if (restStock < 0) {
            throw new InsufficientStockException(
                    "재고가 부족합니다. 상품=" + name + ", 현재 재고=" + stockQuantity + ", 요청 수량=" + quantity);
        }
        this.stockQuantity = restStock;
    }
}
