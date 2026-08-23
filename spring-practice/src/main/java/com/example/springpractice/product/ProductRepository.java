package com.example.springpractice.product;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 비관적 락(SELECT ... FOR UPDATE)으로 상품을 조회한다.
     * 이 트랜잭션이 끝날 때까지 다른 트랜잭션은 같은 row에 대한 조회~갱신을 기다리게 되어,
     * "재고를 실제로 깎는" 경로(주문 생성)에서 동시 요청 간 lost update(오버셀)를 막는다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
