package com.griddynamics.internship.stonksjh.order.exception.exceptions;

public class InvalidStockAmountException extends InvalidDataFormatException {
    
    public InvalidStockAmountException(int amount) {
        super(String.format("Stock amount cannot be nonpositive, was %d", amount));
    }

}
