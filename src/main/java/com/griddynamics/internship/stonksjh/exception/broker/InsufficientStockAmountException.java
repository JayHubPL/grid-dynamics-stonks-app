package com.griddynamics.internship.stonksjh.exception.broker;

import com.griddynamics.internship.stonksjh.model.Order;

public class InsufficientStockAmountException extends RuntimeException {
    
    public InsufficientStockAmountException(int amount, Order.Symbol symbol, long stockCount) {
        super(String.format("Tried to sell %d %s stocks while having %d of them",
                amount,
                symbol.toString(),
                stockCount
        ));
    }

}
