package com.paulo.ecommerceX.domain.dto.productSale;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ProductSaleRequestDTO (@NotNull UUID productId, @NotNull @Positive int quantity){
}
