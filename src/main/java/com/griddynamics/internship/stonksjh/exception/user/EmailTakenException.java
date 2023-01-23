package com.griddynamics.internship.stonksjh.exception.user;

public class EmailTakenException extends RuntimeException {

    public EmailTakenException(String email) {
        super(String.format("User with given email already exists [email=%s]", email));
    }

}
