package com.griddynamics.internship.stonksjh.order.exception.exceptions;

public class InvalidOrderTypeException extends InvalidDataFormatException {
    
    public InvalidOrderTypeException(String orderType) {
        super(String.format("%s in not a valid order type", orderType));
    }

}
