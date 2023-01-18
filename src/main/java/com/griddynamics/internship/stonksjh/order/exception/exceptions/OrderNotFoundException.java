package com.griddynamics.internship.stonksjh.order.exception.exceptions;

import java.util.UUID;

public class OrderNotFoundException extends RuntimeException {
    
    public OrderNotFoundException(UUID uuid) {
        super(String.format("No order with UUID = %s exists", uuid.toString()));
    }

}
