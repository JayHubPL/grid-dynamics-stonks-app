package com.griddynamics.internship.stonksjh.dto.order;

import com.griddynamics.internship.stonksjh.model.Order;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record OrderResponseDTO(
        UUID uuid,
        Order.Type type,
        int amount,
        Order.Symbol symbol,
        Order.Status status,
        BigDecimal bid
) {
}
