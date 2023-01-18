package com.griddynamics.internship.stonksjh.order.exception.exceptions;

public class InvalidSymbolException extends InvalidDataFormatException {

    public InvalidSymbolException(String symbol) {
        super(String.format("Symbol %s is not supported", symbol));
    }
    
}
