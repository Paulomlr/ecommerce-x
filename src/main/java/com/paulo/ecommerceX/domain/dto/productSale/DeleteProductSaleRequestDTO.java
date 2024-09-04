package com.paulo.ecommerceX.domain.dto.productSale;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DeleteProductSaleRequestDTO(@NotNull UUID productId) {
}
