package com.example.kotlinpractice.product

import com.example.kotlinpractice.order.OrderService
import com.example.kotlinpractice.order.dto.OrderResponse
import com.example.kotlinpractice.product.dto.ProductCreateRequest
import com.example.kotlinpractice.product.dto.ProductResponse
import com.example.kotlinpractice.product.dto.ProductUpdateRequest
import jakarta.validation.Valid
import java.net.URI
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productService: ProductService,
    private val orderService: OrderService
) {

    @PostMapping
    fun create(@Valid @RequestBody request: ProductCreateRequest): ResponseEntity<ProductResponse> {
        val response = productService.create(request)
        return ResponseEntity
            .created(URI.create("/api/products/${response.id}"))
            .body(response)
    }

    @GetMapping
    fun findAll(): ResponseEntity<List<ProductResponse>> =
        ResponseEntity.ok(productService.findAll())

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<ProductResponse> =
        ResponseEntity.ok(productService.findById(id))

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: ProductUpdateRequest
    ): ResponseEntity<ProductResponse> =
        ResponseEntity.ok(productService.update(id, request))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        productService.delete(id)
        return ResponseEntity.noContent().build()
    }

    /** 특정 상품이 포함된 주문 내역. */
    @GetMapping("/{id}/orders")
    fun findOrders(@PathVariable id: Long): ResponseEntity<List<OrderResponse>> =
        ResponseEntity.ok(orderService.findByProductId(id))
}
