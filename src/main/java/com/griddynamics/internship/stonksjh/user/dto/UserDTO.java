package com.griddynamics.internship.stonksjh.user.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class UserDTO {

    private UUID uuid;
    private String email;
    private String username;
    private BigDecimal balance;

}
