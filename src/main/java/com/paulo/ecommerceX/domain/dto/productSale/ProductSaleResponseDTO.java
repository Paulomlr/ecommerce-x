package com.paulo.ecommerceX.domain.dto.productSale;

import com.paulo.ecommerceX.domain.ProductSale;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

public record ProductSaleResponseDTO (UUID id, String name, Double price, Integer quantity, Double subTotal) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public ProductSaleResponseDTO(ProductSale productSale){
        this(
                productSale.getProduct().getProductId(),
                productSale.getProduct().getName(),
                productSale.getProduct().getPrice(),
                productSale.getQuantity(),
                productSale.getSubTotal());
    }
}
