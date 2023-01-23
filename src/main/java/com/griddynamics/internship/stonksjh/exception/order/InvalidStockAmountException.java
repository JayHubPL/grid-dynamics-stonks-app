package com.griddynamics.internship.stonksjh.exception.order;

public class InvalidStockAmountException extends RuntimeException {

    public InvalidStockAmountException(int amount) {
        super(String.format("Stock amount cannot be nonpositive, was %d", amount));
    }

}
