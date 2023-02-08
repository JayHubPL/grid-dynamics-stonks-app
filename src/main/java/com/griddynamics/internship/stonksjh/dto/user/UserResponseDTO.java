package com.griddynamics.internship.stonksjh.dto.user;

import com.griddynamics.internship.stonksjh.model.Order;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Builder
public record UserResponseDTO(
        UUID uuid,
        String email,
        String username,
        BigDecimal balance,
        Map<Order.Symbol, Integer> stocks
) {
}
