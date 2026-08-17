# server-practice

백엔드 실무 역량을 쌓기 위한 개인 연습용 워크스페이스입니다. 언어/프레임워크별로 하위 디렉토리를 분리해서 각자 독립된 프로젝트로 관리합니다.

## 구조

```
server-practice/
├── spring-practice/   # Spring Boot (Java) 실습 프로젝트
└── kotlin-practice/   # 같은 도메인을 Kotlin으로 다시 구현한 실습 프로젝트
```

각 하위 디렉토리는 독립적인 빌드 단위(Gradle 프로젝트)이며, 자세한 내용은 각자의 README를 참고하세요.

- [spring-practice/README.md](spring-practice/README.md) — 상품/주문 도메인으로 Spring Boot 실무 패턴(계층 분리, 트랜잭션, N+1 해결 등) 연습
- [kotlin-practice/README.md](kotlin-practice/README.md) — 동일한 도메인을 Kotlin으로 포팅하며 Java+Lombok 대비 달라지는 지점(엔티티 open/no-arg, data class, companion object 팩토리 등) 연습

## 여기서 하는 것

특정 튜토리얼을 그대로 따라 치는 게 아니라, 실무에서 자주 겪는 문제 상황(예: 재고 차감 트랜잭션, N+1 쿼리)을 작은 도메인으로 직접 재현하고 해결하는 방식으로 연습하고 있습니다. 프로젝트가 하나 끝나면 별도 하위 디렉토리로 새 스택을 추가하는 식으로 워크스페이스를 넓혀갈 예정입니다.
