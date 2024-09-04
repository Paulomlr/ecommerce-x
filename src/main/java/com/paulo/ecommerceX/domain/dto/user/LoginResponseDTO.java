package com.paulo.ecommerceX.domain.dto.user;

import java.util.UUID;

public record LoginResponseDTO(UUID id, String token) {
}
