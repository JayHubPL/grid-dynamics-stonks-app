package com.griddynamics.internship.stonksjh.order.dto;

import java.util.UUID;

import org.springframework.hateoas.RepresentationModel;

import com.griddynamics.internship.stonksjh.order.model.OrderType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO extends RepresentationModel<OrderDTO>{
    
    private UUID uuid;
    private OrderType orderType;
    private int amount;
    private String symbol;

}
