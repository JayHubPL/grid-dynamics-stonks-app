package com.griddynamics.internship.stonksjh.exception.trading;

public class MissingFinnhubApiTokenException extends RuntimeException {
    
    public MissingFinnhubApiTokenException() {
        super("Missing finnhub.io API token");
    }

}
