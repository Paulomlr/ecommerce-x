package com.paulo.ecommerceX.domain.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductRequestDTO(@NotBlank String name,
                                @NotNull @Positive Double price,
                                @NotNull @Positive Integer stockQuantity) {
}
