package com.griddynamics.internship.stonksjh.exception.user;

public class UsernameTakenException extends RuntimeException {

    public UsernameTakenException(String username) {
        super(String.format("User with given username already exists [username=%s]", username));
    }

}
