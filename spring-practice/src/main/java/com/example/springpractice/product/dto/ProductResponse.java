package com.example.springpractice.product.dto;

import com.example.springpractice.product.Product;

public record ProductResponse(
        Long id,
        String name,
        int price,
        int stockQuantity
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStockQuantity()
        );
    }
}
