package com.griddynamics.internship.stonksjh.exception.user;

public class EmailFormatException extends RuntimeException {

    public EmailFormatException(String email) {
        super(String.format("%s - this is not a correct email", email));
    }

}
