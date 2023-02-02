package com.griddynamics.internship.stonksjh.exception.broker;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class InsufficientBalanceException extends RuntimeException {
    
    public InsufficientBalanceException(BigDecimal orderValue, BigDecimal balance) {
        super(String.format("Tried to buy stocks worth %s while actual balance stands at %s",
                NumberFormat.getCurrencyInstance(Locale.US).format(orderValue),
                NumberFormat.getCurrencyInstance(Locale.US).format(balance)
        ));
    }

}
