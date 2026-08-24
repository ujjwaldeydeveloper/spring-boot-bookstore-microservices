package com.example.bookstore.catalog.domain;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super(message);
    }

    // factory function
    public static ProductNotFoundException forCode(String code) {
        return new ProductNotFoundException(
                "Product with code" + code + "not found");
    }
}
