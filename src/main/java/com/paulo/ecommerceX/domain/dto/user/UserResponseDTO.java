package com.paulo.ecommerceX.domain.dto.user;

import com.paulo.ecommerceX.domain.User;

import java.util.UUID;

public record UserResponseDTO(UUID id) {
    public UserResponseDTO(User user) {
        this(user.getUserId());
    }
}
