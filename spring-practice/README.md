# Spring Practice

Spring Boot로 상품/주문 도메인을 구현하며 실무에서 자주 마주치는 문제들을 연습한 토이 프로젝트입니다.

## 스택

- Spring Boot 3.5.14 (Java 17)
- Spring Data JPA, Spring Web, Bean Validation
- H2 (인메모리 DB)
- Lombok
- JUnit 5

## 연습한 내용

### 1. 상품(Product) CRUD 기본기
- Controller / Service / Repository 계층 분리
- DTO 분리 (`ProductCreateRequest` / `ProductUpdateRequest` / `ProductResponse`)로 엔티티를 API에 직접 노출하지 않기
- `@RestControllerAdvice` + 커스텀 예외(`NotFoundException`)로 전역 예외 처리 및 일관된 에러 응답(`ErrorResponse`) 만들기
- `@Valid` 기반 요청 검증과 검증 실패 시 400 응답 처리

### 2. 주문 생성 + 재고 차감 트랜잭션
- 주문 항목(`OrderItem`) 생성 시 상품 재고를 함께 차감하는 로직을 하나의 `@Transactional`로 묶어, 항목 중 하나라도 재고 부족(`InsufficientStockException`)이면 이미 차감된 재고까지 전부 롤백되도록 구성
- `Order` ↔ `OrderItem` 연관관계와 cascade 저장 (`orderRepository.save(order)` 한 번으로 항목까지 저장)
- 클래스 레벨 `@Transactional(readOnly = true)` + 쓰기 메서드에만 `@Transactional`을 얹는 패턴으로 조회 성능 최적화

### 3. 주문/상품별 조회 + N+1 문제 해결
- `Order` 목록 조회 시 `orderItems`, `product`를 지연 로딩으로 접근하면 N+1 쿼리가 발생하는 상황을 직접 재현
- JPQL `join fetch`로 주문 + 주문항목 + 상품을 한 번의 쿼리로 로딩해 N+1 해결
- 컬렉션 조인 시 발생하는 row 중복을 `distinct`로 제거
- 서브쿼리를 활용해 "특정 상품이 포함된 주문 목록"처럼 조건은 걸되 연관 데이터는 전체를 fetch join으로 가져오는 쿼리 작성
- `OrderQueryTest`에서 실제 쿼리 결과 검증

### 4. 재고 차감 동시성 제어
- 기존 주문 생성 로직은 `@Transactional`로 "한 요청 안에서 일부만 반영되는 문제"는 막았지만, **서로 다른 두 요청이 동시에 같은 상품을 주문하면** 트랜잭션 격리와 무관하게 둘 다 같은 재고 값을 읽고 각자 차감해 저장하는 lost update(오버셀)가 발생한다.
- `StockConcurrencyTest`에서 스레드 30개로 재고 10개짜리 상품을 동시에 주문시켜 버그를 직접 재현: 수정 전에는 재고 부족 예외가 하나도 발생하지 않고 **30건이 전부 성공**했다(재고 10개짜리 상품이 30개 팔린 셈).
- **비관적 락**으로 실제 주문 플로우 수정: `ProductRepository.findByIdForUpdate()`에 `@Lock(PESSIMISTIC_WRITE)`를 걸어 `SELECT ... FOR UPDATE`로 조회하고, `OrderService.create()`에서 재고를 깎는 조회만 이 메서드로 교체. 단순 조회 API는 그대로 락 없는 조회를 유지 — 락은 "재고를 실제로 깎는" 경로에만 건다. 적용 후 같은 테스트를 반복 실행해도 항상 성공 10건 / 실패(재고부족) 20건 / 최종 재고 0으로 안정적으로 통과.
- **낙관적 락**은 비교용으로 별도 데모: `Product`에 `@Version`을 추가하고, 두 스레드가 `CyclicBarrier`로 같은 상품을 동시에 조회한 뒤 각자 저장하도록 만들어 나중에 커밋하는 쪽에서 `ObjectOptimisticLockingFailureException`이 발생하는 것을 검증(`StockConcurrencyTest#optimisticLock_conflictOnConcurrentUpdate`).
- **왜 재고 차감에는 비관적 락을 골랐나**: 인기 상품일수록 같은 row에 컨텐션이 몰리고, 체크아웃 흐름은 실패 시 사용자에게 즉시 응답해야 하므로 "충돌 시 재시도"보다 "충돌 자체를 막는" 편이 낫다고 판단. 낙관적 락은 충돌이 드물고 클라이언트가 재시도할 수 있는 리소스(예: 게시글 동시 수정)에 더 적합하다.
- **다음 연습 후보**: 여러 서버 인스턴스로 확장하면 DB 락만으로는 부족해지므로(각 인스턴스는 자기 트랜잭션 안에서만 락을 보장) Redis 분산락(Redisson 등)으로 인스턴스 간 동시성까지 제어하는 것을 다음 단계로 남겨둔다.

## 커밋 히스토리로 보는 진행 순서

1. `chore: Spring Boot 프로젝트 초기 세팅 (Gradle, JPA, H2)`
2. `feat: 상품 CRUD 구현`
3. `feat: 주문 생성 + 재고 차감 트랜잭션 구현`
4. `feat: 주문/상품별 조회 + N+1 해결`
5. `feat: 재고 차감 동시성 제어 (비관적 락) + 낙관적 락 비교 데모`

## 실행

```bash
./gradlew bootRun
```

## 테스트

```bash
./gradlew test
```
