package com.griddynamics.internship.stonksjh.order.dto;

import lombok.Data;

@Data
public class CrudRequestDTO {

    private String symbol;
    private int amount;
    private String orderType;
    
}
