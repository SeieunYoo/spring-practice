package com.example.kotlinpractice.product

import com.example.kotlinpractice.common.exception.NotFoundException
import com.example.kotlinpractice.product.dto.ProductCreateRequest
import com.example.kotlinpractice.product.dto.ProductUpdateRequest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired
    private lateinit var productService: ProductService

    @Test
    @DisplayName("상품을 등록하면 id가 부여되고 입력값이 그대로 저장된다")
    fun create() {
        val created = productService.create(ProductCreateRequest("티셔츠", 19000, 10))

        assertThat(created.id).isNotNull()
        assertThat(created.name).isEqualTo("티셔츠")
        assertThat(created.price).isEqualTo(19000)
        assertThat(created.stockQuantity).isEqualTo(10)
    }

    @Test
    @DisplayName("상품을 수정하면 변경 내용이 반영된다")
    fun update() {
        val created = productService.create(ProductCreateRequest("티셔츠", 19000, 10))

        val updated = productService.update(
            created.id!!, ProductUpdateRequest("후드티", 39000, 5)
        )

        assertThat(updated.name).isEqualTo("후드티")
        assertThat(updated.price).isEqualTo(39000)
        assertThat(updated.stockQuantity).isEqualTo(5)
    }

    @Test
    @DisplayName("상품을 삭제하면 더 이상 조회되지 않는다")
    fun delete() {
        val created = productService.create(ProductCreateRequest("티셔츠", 19000, 10))

        productService.delete(created.id!!)

        assertThatThrownBy { productService.findById(created.id!!) }
            .isInstanceOf(NotFoundException::class.java)
    }

    @Test
    @DisplayName("존재하지 않는 상품을 조회하면 NotFoundException 이 발생한다")
    fun findById_notFound() {
        assertThatThrownBy { productService.findById(999L) }
            .isInstanceOf(NotFoundException::class.java)
    }
}
