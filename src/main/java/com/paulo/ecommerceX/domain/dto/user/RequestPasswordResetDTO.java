package com.paulo.ecommerceX.domain.dto.user;

import jakarta.validation.constraints.NotBlank;

public record RequestPasswordResetDTO(@NotBlank String login) {
}
