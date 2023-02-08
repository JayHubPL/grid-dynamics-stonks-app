package com.griddynamics.internship.stonksjh.dto.order;

import java.math.BigDecimal;

import lombok.Builder;

@Builder
public record OrderCreateRequestDTO(
        String symbol,
        int amount,
        String type,
        BigDecimal bid
) {
}
