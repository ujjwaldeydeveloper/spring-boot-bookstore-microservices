package com.example.bookstore.catalog.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
// only load partial component ; faster;
@DataJpaTest(properties = {
        "spring.test.database.replace=none",
        "spring.datasource.url=jdbc:tc:postgresql:16-alpine:///db",
})
@Sql("/test-data.sql")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldGetAllProducts() {
        List<ProductEntity> products = productRepository.findAll();
        assertEquals(14, products.size());
    }

    @Test
    void shouldGetProductByCode() {
        ProductEntity product = productRepository.findByCode("TEST-01")
                .orElseThrow();

        assertEquals("TEST-01", product.getCode());
        assertEquals("Product 01", product.getName());
        assertEquals("Test product 1", product.getDescription());
        assertEquals("https://example.com/products/1.jpg",
                product.getImageUrl());
        assertEquals(new BigDecimal("1"), product.getPrice());
    }

    @Test
    void shouldReturnEmptyWhenProductCodeNotExist() {
        assertTrue(productRepository.findByCode("invalid_product_code").isEmpty());;
    }
}
