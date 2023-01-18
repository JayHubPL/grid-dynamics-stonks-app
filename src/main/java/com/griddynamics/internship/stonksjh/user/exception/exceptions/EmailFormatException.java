package com.griddynamics.internship.stonksjh.user.exception.exceptions;

public class EmailFormatException extends DataFormatException {

    public EmailFormatException(String email) {
        super(String.format("%s - this is not a correct email", email));
    }

}
