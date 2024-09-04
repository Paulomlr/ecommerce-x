package com.paulo.ecommerceX.domain.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequestDTO(
        @NotBlank @Size(min = 5, max = 35) String login,
        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters long") String password) {
}
