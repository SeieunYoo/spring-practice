package com.example.springpractice.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.springpractice.order.dto.OrderCreateRequest;
import com.example.springpractice.order.dto.OrderCreateRequest.OrderItemRequest;
import com.example.springpractice.product.Product;
import com.example.springpractice.product.ProductRepository;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 재고 차감의 동시성 문제를 실제 스레드로 재현/검증한다.
 * 커밋된 DB 상태를 관찰해야 하므로 클래스 레벨 @Transactional을 두지 않는다.
 */
@SpringBootTest
class StockConcurrencyTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Product saveProduct(String name, int price, int stock) {
        return productRepository.save(
                Product.builder().name(name).price(price).stockQuantity(stock).build());
    }

    @Test
    @DisplayName("동시에 여러 건 주문해도 재고보다 많이 팔리지 않는다 (비관적 락)")
    void concurrentOrders_doNotOversellStock() throws InterruptedException {
        Product product = saveProduct("한정판 스니커즈", 150000, 10);

        int threadCount = 30;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    orderService.create(new OrderCreateRequest(
                            "고객", List.of(new OrderItemRequest(product.getId(), 1))));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        Product after = productRepository.findById(product.getId()).orElseThrow();

        // 재고(10개)보다 많이 팔리면 안 되고, 재고는 절대 음수가 되면 안 된다.
        assertThat(successCount.get()).isEqualTo(10);
        assertThat(failCount.get()).isEqualTo(20);
        assertThat(after.getStockQuantity()).isZero();
    }

    /**
     * 비관적 락(SELECT ... FOR UPDATE)과 달리, 낙관적 락은 조회 시점에는 아무도 막지 않고
     * "먼저 커밋한 쪽이 이긴다" — 나중에 커밋하는 트랜잭션은 버전이 이미 바뀐 걸 발견하고
     * ObjectOptimisticLockingFailureException으로 실패한다(재시도는 호출자 책임).
     * 실제 주문 생성(OrderService.create)은 비관적 락을 쓰므로, 여기서는 그 경로를 타지 않고
     * 락 없는 조회(findById) + save를 직접 두 트랜잭션으로 동시에 실행해 버전 충돌을 재현한다.
     */
    @Test
    @DisplayName("락 없이 동시에 갱신하면 낙관적 락 버전 충돌로 나중에 커밋하는 쪽이 실패한다")
    void optimisticLock_conflictOnConcurrentUpdate() throws InterruptedException {
        Product product = saveProduct("한정판 후드티", 89000, 5);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        CyclicBarrier barrier = new CyclicBarrier(2);
        AtomicInteger failureCount = new AtomicInteger();

        Runnable decreaseStockWithoutLock = () -> {
            try {
                transactionTemplate.executeWithoutResult(status -> {
                    Product found = productRepository.findById(product.getId()).orElseThrow();
                    awaitQuietly(barrier); // 두 트랜잭션이 같은 버전을 읽을 때까지 대기시켜 충돌을 강제한다
                    found.removeStock(1);
                    productRepository.saveAndFlush(found);
                });
            } catch (ObjectOptimisticLockingFailureException e) {
                failureCount.incrementAndGet();
            }
        };

        Thread t1 = new Thread(decreaseStockWithoutLock);
        Thread t2 = new Thread(decreaseStockWithoutLock);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // 둘 중 하나는 성공하고, 나머지 하나는 버전 충돌로 실패한다.
        assertThat(failureCount.get()).isEqualTo(1);
    }

    private void awaitQuietly(CyclicBarrier barrier) {
        try {
            barrier.await(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
