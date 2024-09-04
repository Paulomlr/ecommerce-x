package com.paulo.ecommerceX.domain.dto.user;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(@NotBlank String login, @NotBlank String password) {
}
