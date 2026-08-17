package com.example.kotlinpractice.order

import com.example.kotlinpractice.order.dto.OrderCreateRequest
import com.example.kotlinpractice.order.dto.OrderResponse
import jakarta.validation.Valid
import java.net.URI
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService
) {

    @PostMapping
    fun create(@Valid @RequestBody request: OrderCreateRequest): ResponseEntity<OrderResponse> {
        val response = orderService.create(request)
        return ResponseEntity
            .created(URI.create("/api/orders/${response.orderId}"))
            .body(response)
    }

    @GetMapping
    fun findAll(): ResponseEntity<List<OrderResponse>> =
        ResponseEntity.ok(orderService.findAll())

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<OrderResponse> =
        ResponseEntity.ok(orderService.findById(id))
}
