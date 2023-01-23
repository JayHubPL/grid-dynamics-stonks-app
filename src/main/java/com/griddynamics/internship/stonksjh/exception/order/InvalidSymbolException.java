package com.griddynamics.internship.stonksjh.exception.order;

public class InvalidSymbolException extends RuntimeException {

    public InvalidSymbolException(String symbol) {
        super(String.format("Symbol %s is not supported", symbol));
    }

}
