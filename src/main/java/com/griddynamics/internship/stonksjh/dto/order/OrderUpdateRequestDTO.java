package com.griddynamics.internship.stonksjh.dto.order;

import java.math.BigDecimal;

import lombok.Builder;

@Builder
public record OrderUpdateRequestDTO(
        String symbol,
        int amount,
        BigDecimal bid
) {
}
