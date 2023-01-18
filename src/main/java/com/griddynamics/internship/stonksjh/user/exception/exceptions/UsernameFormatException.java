package com.griddynamics.internship.stonksjh.user.exception.exceptions;

public class UsernameFormatException extends DataFormatException {

    public UsernameFormatException(String username) {
        super(String.format("%s - invalid username, only letters and _ are allowed", username));
    }

}
