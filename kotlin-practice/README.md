# Kotlin Practice

`spring-practice`(Spring Boot + Java + Lombok)에서 만든 상품/주문 도메인을 Kotlin으로 다시 구현한 토이 프로젝트입니다. 기능은 동일하게 1:1로 포팅하고, Java+Lombok 코드가 Kotlin에서는 어떻게 달라지는지에 집중했습니다.

## 스택

- Spring Boot 3.5.14 (Kotlin 1.9.25, Java 17 toolchain)
- Spring Data JPA, Spring Web, Bean Validation
- H2 (인메모리 DB)
- kotlin-jpa / kotlin-spring 컴파일러 플러그인 (Lombok 대체)
- JUnit 5, AssertJ

## 연습한 내용

spring-practice와 동일한 범위(상품 CRUD, 주문 생성+재고 차감 트랜잭션, fetch join 기반 N+1 해결)를 Kotlin으로 다시 구현했습니다. 자세한 도메인 설명은 [spring-practice/README.md](../spring-practice/README.md)를 참고하세요.

## Java(+Lombok) 대비 Kotlin에서 달라진 점

- **엔티티 open/no-arg 문제**: JPA는 프록시 생성을 위해 엔티티 클래스가 상속 가능(open)해야 하고, 리플렉션으로 값을 채우기 위한 파라미터 없는 생성자가 필요합니다. Java+Lombok에서는 `@NoArgsConstructor(PROTECTED)`로 직접 만들었지만, Kotlin은 기본적으로 클래스가 `final`이라 별도 처리가 없으면 JPA가 동작하지 않습니다. `kotlin("plugin.jpa")` 컴파일러 플러그인이 `@Entity` 클래스를 자동으로 open 처리하고 합성 no-arg 생성자를 넣어줘서 해결했습니다(`plugin.spring`은 `@Service`/`@Component` 등에 대해 같은 역할).
- **정적 팩토리 → companion object**: `Order.create()`, `OrderItem.create()`, `Product.builder()`처럼 생성자를 감추고 의미 있는 이름의 생성 메서드만 노출하던 패턴을, Kotlin에서는 `private constructor` + `companion object` 팩토리 함수로 옮겼습니다.
- **Lombok `@Getter` 제거**: Kotlin은 `val`/`var` 프로퍼티 선언만으로 getter(+ 필요시 setter)가 자동 생성되므로 Lombok이 통째로 필요 없어졌습니다. 대신 외부에서 값을 바꾸면 안 되는 필드(`id`, `orderedAt` 등)는 `var x = ... ; protected set`으로 세터 가시성만 제한했습니다.
- **양방향 연관관계 편의 메서드의 가시성**: Java의 package-private(`void assignOrder(...)`)에 대응하는 접근제한자가 Kotlin에는 없어서 `internal`로 옮겼습니다(패키지 단위가 아니라 모듈 단위로 범위가 넓어진다는 차이가 있습니다).
- **레코드 → data class**: `OrderCreateRequest`, `OrderResponse` 같은 DTO(Java record)는 Kotlin `data class`로 직역했습니다.
- **Bean Validation 어노테이션 타겟**: `data class` 생성자 프로퍼티에 `@NotBlank`, `@Positive` 같은 검증 어노테이션을 그냥 붙이면 기본적으로 생성자 파라미터에만 적용되고, Spring이 `@RequestBody`를 역직렬화할 때 실제로 검증되는 필드에는 적용되지 않습니다. `@field:NotBlank`처럼 use-site target을 명시해야 의도대로 동작합니다. JPA 어노테이션(`@Column`, `@Id` 등)은 필드 타겟만 지원해서 별도 지정 없이도 자동으로 필드에 적용됩니다.
- **`Optional` → nullable 타입**: 리포지토리 단건 조회에서 Java의 `Optional<Order>` 대신 Kotlin다운 `Order?`를 사용하고, `?: throw ...`로 처리했습니다.

## 실행

```bash
./gradlew bootRun
```

## 테스트

```bash
./gradlew test
```
