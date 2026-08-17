package com.example.kotlinpractice.order

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface OrderRepository : JpaRepository<Order, Long> {

    /*
     * N+1 주의:
     * 단순히 findAll() 후 응답 변환에서 order.orderItems, orderItem.product 를 접근하면
     *   - 주문 목록 1번
     *   - 주문마다 orderItems 조회 N번
     *   - orderItem마다 product 조회 M번
     * 까지 쿼리가 폭증한다(N+1).
     *
     * 아래처럼 fetch join 으로 주문 + 주문항목 + 상품을 한 번에 로딩해 해결한다.
     * 컬렉션을 조인하면 row 가 항목 수만큼 곱해지므로 distinct 로 엔티티 중복을 제거한다.
     */
    @Query(
        "select distinct o from Order o " +
            "join fetch o.orderItems oi " +
            "join fetch oi.product " +
            "order by o.id desc"
    )
    fun findAllWithItems(): List<Order>

    @Query(
        "select o from Order o " +
            "join fetch o.orderItems oi " +
            "join fetch oi.product " +
            "where o.id = :id"
    )
    fun findByIdWithItems(@Param("id") id: Long): Order?

    /*
     * 특정 상품이 포함된 주문 목록.
     * where 절은 해당 상품을 포함하는 주문으로 걸러내되,
     * fetch join 으로 그 주문의 "전체" 항목과 상품을 함께 로딩한다(주문 내역 전체를 보여주기 위함).
     */
    @Query(
        "select distinct o from Order o " +
            "join fetch o.orderItems oi " +
            "join fetch oi.product " +
            "where o.id in (" +
            "  select oi2.order.id from OrderItem oi2 where oi2.product.id = :productId" +
            ") " +
            "order by o.id desc"
    )
    fun findAllByProductId(@Param("productId") productId: Long): List<Order>
}
