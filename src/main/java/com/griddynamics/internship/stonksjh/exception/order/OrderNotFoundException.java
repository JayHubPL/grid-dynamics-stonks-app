package com.griddynamics.internship.stonksjh.exception.order;

import java.util.UUID;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(UUID uuid) {
        super(String.format("No order with UUID = %s exists", uuid.toString()));
    }

}
