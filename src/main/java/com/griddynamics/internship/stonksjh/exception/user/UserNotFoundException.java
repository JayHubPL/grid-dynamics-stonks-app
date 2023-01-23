package com.griddynamics.internship.stonksjh.exception.user;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID uuid) {
        super(String.format("User with given uuid doesn't exist [uuid=%s]", uuid.toString()));
    }

}
