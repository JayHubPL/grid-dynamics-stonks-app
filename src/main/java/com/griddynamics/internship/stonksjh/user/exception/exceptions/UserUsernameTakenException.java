package com.griddynamics.internship.stonksjh.user.exception.exceptions;

public class UserUsernameTakenException extends ConflictException {

    public UserUsernameTakenException(String username) {
        super(String.format("User with given username already exists [username=%s]", username));
    }

}
