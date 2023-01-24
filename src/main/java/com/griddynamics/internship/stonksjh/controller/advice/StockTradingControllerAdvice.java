package com.griddynamics.internship.stonksjh.controller.advice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.griddynamics.internship.stonksjh.controller.StockTradingController;
import com.griddynamics.internship.stonksjh.exception.ApiExceptionDTO;
import com.griddynamics.internship.stonksjh.exception.order.OrderNotFoundException;
import com.griddynamics.internship.stonksjh.exception.user.UserNotFoundException;

@RestControllerAdvice(basePackageClasses = StockTradingController.class)
public class StockTradingControllerAdvice {
    
    @ExceptionHandler({
            UserNotFoundException.class,
            OrderNotFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiExceptionDTO handleUserNotFoundException(Exception e) {
        return ApiExceptionDTO.of(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiExceptionDTO handleIllegalArgumentException(Exception e) {
        return ApiExceptionDTO.of(e.getMessage());
    }

}
