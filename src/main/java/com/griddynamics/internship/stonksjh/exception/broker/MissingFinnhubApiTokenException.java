package com.griddynamics.internship.stonksjh.exception.broker;

public class MissingFinnhubApiTokenException extends RuntimeException {
    
    public MissingFinnhubApiTokenException() {
        super("Missing finnhub.io API token");
    }

}
