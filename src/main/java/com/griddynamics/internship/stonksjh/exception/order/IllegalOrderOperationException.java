package com.griddynamics.internship.stonksjh.exception.order;

import java.util.UUID;

public class IllegalOrderOperationException extends RuntimeException {

    public IllegalOrderOperationException(UUID uuid) {
        super(String.format("Cannot update/delete order with UUID %s because it is completed",
                uuid.toString()
        ));
    }
    
}
