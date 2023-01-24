package com.griddynamics.internship.stonksjh.exception.order;

public class InvalidOrderTypeException extends RuntimeException {

    public InvalidOrderTypeException(String type) {
        super(String.format("%s in not a valid order type", type));
    }

}
