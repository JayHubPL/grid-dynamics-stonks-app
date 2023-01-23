package com.griddynamics.internship.stonksjh.dto.user;

import lombok.Builder;

@Builder
public record UserRequestDTO(
        String email,
        String username
) {
}
