package com.griddynamics.internship.stonksjh.dto.user;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record UserResponseDTO(
        UUID uuid,
        String email,
        String username,
        BigDecimal balance
) {
}
