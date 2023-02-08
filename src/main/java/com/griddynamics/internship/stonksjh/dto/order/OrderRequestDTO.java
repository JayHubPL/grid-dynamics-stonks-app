package com.griddynamics.internship.stonksjh.dto.order;

import lombok.Builder;

@Builder
public record OrderRequestDTO(
        String symbol,
        int amount,
        String type
) {
}
