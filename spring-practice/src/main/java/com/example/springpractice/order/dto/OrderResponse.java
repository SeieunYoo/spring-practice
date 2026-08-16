package com.example.springpractice.order.dto;

import com.example.springpractice.order.Order;
import com.example.springpractice.order.OrderItem;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long orderId,
        String ordererName,
        LocalDateTime orderedAt,
        List<OrderItemResponse> items,
        int totalAmount
) {
    public static OrderResponse from(Order order) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(OrderItemResponse::from)
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getOrdererName(),
                order.getOrderedAt(),
                items,
                order.getTotalAmount()
        );
    }

    public record OrderItemResponse(
            Long productId,
            String productName,
            int quantity,
            int orderPrice,
            int subtotal
    ) {
        public static OrderItemResponse from(OrderItem orderItem) {
            return new OrderItemResponse(
                    orderItem.getProduct().getId(),
                    orderItem.getProduct().getName(),
                    orderItem.getQuantity(),
                    orderItem.getOrderPrice(),
                    orderItem.getSubtotal()
            );
        }
    }
}
