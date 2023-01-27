package com.griddynamics.internship.stonksjh.dto.order;

import lombok.Builder;

@Builder
public record OrderCreateRequestDTO(
        String symbol,
        int amount,
        String type
) {
}
