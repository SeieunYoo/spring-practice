package com.example.springpractice.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record OrderCreateRequest(
        @NotBlank(message = "주문자 이름은 필수입니다.")
        String ordererName,

        @NotEmpty(message = "주문 항목은 1개 이상이어야 합니다.")
        @Valid
        List<OrderItemRequest> items
) {
    public record OrderItemRequest(
            @NotNull(message = "상품 id는 필수입니다.")
            Long productId,

            @Positive(message = "주문 수량은 1 이상이어야 합니다.")
            int quantity
    ) {
    }
}
