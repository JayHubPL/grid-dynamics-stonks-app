package com.griddynamics.internship.stonksjh.exception.order;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class InvalidBidException extends RuntimeException {
    
    public InvalidBidException(BigDecimal bid) {
        super(String.format("Bid cannot be nonpositive, was %s",
                NumberFormat.getCurrencyInstance(Locale.US).format(bid)
        ));
    }

}
