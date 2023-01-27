package com.griddynamics.internship.stonksjh.dto.order;

import lombok.Builder;

@Builder
public record OrderUpdateRequestDTO(
        String symbol,
        int amount
) {
}
