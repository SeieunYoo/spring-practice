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

## 커밋 히스토리로 보는 진행 순서

1. `chore: Spring Boot 프로젝트 초기 세팅 (Gradle, JPA, H2)`
2. `feat: 상품 CRUD 구현`
3. `feat: 주문 생성 + 재고 차감 트랜잭션 구현`
4. `feat: 주문/상품별 조회 + N+1 해결`

## 실행

```bash
./gradlew bootRun
```

## 테스트

```bash
./gradlew test
```
