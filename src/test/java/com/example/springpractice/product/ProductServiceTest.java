package com.example.springpractice.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.springpractice.common.exception.NotFoundException;
import com.example.springpractice.product.dto.ProductCreateRequest;
import com.example.springpractice.product.dto.ProductResponse;
import com.example.springpractice.product.dto.ProductUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Test
    @DisplayName("상품을 등록하면 id가 부여되고 입력값이 그대로 저장된다")
    void create() {
        ProductResponse created = productService.create(
                new ProductCreateRequest("티셔츠", 19000, 10));

        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("티셔츠");
        assertThat(created.price()).isEqualTo(19000);
        assertThat(created.stockQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("상품을 수정하면 변경 내용이 반영된다")
    void update() {
        ProductResponse created = productService.create(
                new ProductCreateRequest("티셔츠", 19000, 10));

        ProductResponse updated = productService.update(
                created.id(), new ProductUpdateRequest("후드티", 39000, 5));

        assertThat(updated.name()).isEqualTo("후드티");
        assertThat(updated.price()).isEqualTo(39000);
        assertThat(updated.stockQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("상품을 삭제하면 더 이상 조회되지 않는다")
    void delete() {
        ProductResponse created = productService.create(
                new ProductCreateRequest("티셔츠", 19000, 10));

        productService.delete(created.id());

        assertThatThrownBy(() -> productService.findById(created.id()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 상품을 조회하면 NotFoundException 이 발생한다")
    void findById_notFound() {
        assertThatThrownBy(() -> productService.findById(999L))
                .isInstanceOf(NotFoundException.class);
    }
}
