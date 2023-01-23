package com.griddynamics.internship.stonksjh.exception.user;

public class UsernameFormatException extends RuntimeException {

    public UsernameFormatException(String username) {
        super(String.format("%s - invalid username, only letters and _ are allowed", username));
    }

}
