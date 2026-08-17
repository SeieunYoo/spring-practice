package com.example.kotlinpractice.product

import com.example.kotlinpractice.common.exception.NotFoundException
import com.example.kotlinpractice.product.dto.ProductCreateRequest
import com.example.kotlinpractice.product.dto.ProductResponse
import com.example.kotlinpractice.product.dto.ProductUpdateRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ProductService(
    private val productRepository: ProductRepository
) {

    @Transactional
    fun create(request: ProductCreateRequest): ProductResponse {
        val saved = productRepository.save(request.toEntity())
        return ProductResponse.from(saved)
    }

    fun findAll(): List<ProductResponse> =
        productRepository.findAll().map { ProductResponse.from(it) }

    fun findById(id: Long): ProductResponse =
        ProductResponse.from(getProductOrThrow(id))

    @Transactional
    fun update(id: Long, request: ProductUpdateRequest): ProductResponse {
        val product = getProductOrThrow(id)
        // 더티 체킹: 트랜잭션 종료 시점에 변경 감지로 자동 UPDATE
        product.update(request.name, request.price, request.stockQuantity)
        return ProductResponse.from(product)
    }

    @Transactional
    fun delete(id: Long) {
        val product = getProductOrThrow(id)
        productRepository.delete(product)
    }

    private fun getProductOrThrow(id: Long): Product =
        productRepository.findById(id)
            .orElseThrow { NotFoundException("상품을 찾을 수 없습니다. id=$id") }
}
