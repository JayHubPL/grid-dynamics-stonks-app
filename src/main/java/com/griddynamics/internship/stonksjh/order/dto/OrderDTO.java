package com.griddynamics.internship.stonksjh.order.dto;

import java.util.UUID;

import org.springframework.hateoas.RepresentationModel;

import com.griddynamics.internship.stonksjh.order.model.OrderType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class OrderDTO extends RepresentationModel<OrderDTO>{
    
    private UUID uuid;
    private OrderType orderType;
    private int amount;
    private String symbol;

}
