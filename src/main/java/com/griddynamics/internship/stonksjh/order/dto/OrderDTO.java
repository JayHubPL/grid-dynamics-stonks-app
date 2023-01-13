package com.griddynamics.internship.stonksjh.order.dto;

import java.util.UUID;

import com.griddynamics.internship.stonksjh.order.model.OrderType;
import com.griddynamics.internship.stonksjh.order.model.Symbol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    
    private UUID uuid;
    private OrderType orderType;
    private int amount;
    private Symbol symbol;

}
