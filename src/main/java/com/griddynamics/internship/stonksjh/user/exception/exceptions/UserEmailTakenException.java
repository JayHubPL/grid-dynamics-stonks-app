package com.griddynamics.internship.stonksjh.user.exception.exceptions;

public class UserEmailTakenException extends ConflictException {

    public UserEmailTakenException(String email) {
        super(String.format("User with given email already exists [email=%s]", email));
    }

}
