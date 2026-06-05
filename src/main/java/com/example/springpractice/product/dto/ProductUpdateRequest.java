package com.example.springpractice.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductUpdateRequest(
        @NotBlank(message = "상품명은 필수입니다.")
        String name,

        @PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
        int price,

        @PositiveOrZero(message = "재고 수량은 0 이상이어야 합니다.")
        int stockQuantity
) {
}
