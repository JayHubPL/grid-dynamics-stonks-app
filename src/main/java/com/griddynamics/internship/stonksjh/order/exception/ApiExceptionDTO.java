package com.griddynamics.internship.stonksjh.order.exception;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class ApiExceptionDTO {

    private final String message;
    private final ZonedDateTime timestamp;

    public ApiExceptionDTO(String message) {
        this.message = message;
        timestamp = ZonedDateTime.now();
    }

}