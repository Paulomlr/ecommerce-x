package com.paulo.ecommerceX.domain.dto.product;

import com.paulo.ecommerceX.domain.Product;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

public record ProductResponseDTO (UUID id, String name, Double price, Integer stockQuantity) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public ProductResponseDTO(Product product){
        this(product.getProductId(), product.getName(), product.getPrice(), product.getStockQuantity());
    }
}
