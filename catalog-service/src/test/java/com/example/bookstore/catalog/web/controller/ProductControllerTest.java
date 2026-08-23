package com.example.bookstore.catalog.web.controller;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.example.bookstore.catalog.AbstractIT;
import com.example.bookstore.catalog.domain.Product;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

@Sql("/test-data.sql")
class ProductControllerTest extends AbstractIT {

    @Test
    void shouldReturnProducts() {
        given().contentType(ContentType.JSON)
                .when()
                .get("/api/products")
                .then()
                .statusCode(200)
                .body("data", hasSize(10))
                .body("totalElements", is(14))
                .body("pageNumber", is(1))
                .body("totalPages", is(2))
                .body("isFirst", is(true))
                .body("isLast", is(false))
                .body("hasNext", is(true))
                .body("hasPrevious", is(false));
    }

    @Test
    void shouldReturnProductByCode() {
        Product product = given().contentType(ContentType.JSON)
                .when()
                .get("/api/products/TEST-01")
                .then()
                .statusCode(200)
                .extract()
                .as(Product.class);

        Product expectedProduct = new Product(
                "TEST-01",
                "Product 01",
                "Test product 1",
                "https://example.com/products/1.jpg",
                new BigDecimal("1"));
        assertThat(product).isEqualTo(expectedProduct);
    }

    @Test
    void shouldReturnNotFoundWhenProductCodeNotExist() {
        String code = "invalid_product_code";
        given().contentType(ContentType.JSON)
                .when()
                .get("/api/products/{code}", code)
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("type",
                        is("https://api.bookstore.com/errors/not-found"))
                .body("title", is("Product Not Found"))
                .body("status", is(404))
                .body("detail", is("Product with code" + code + "not found"));
    }
}
