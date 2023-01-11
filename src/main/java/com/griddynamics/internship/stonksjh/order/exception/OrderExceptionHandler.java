package com.griddynamics.internship.stonksjh.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.griddynamics.internship.stonksjh.order.controller.OrderCrudController;
import com.griddynamics.internship.stonksjh.order.exception.exceptions.InvalidStockAmountException;
import com.griddynamics.internship.stonksjh.order.exception.exceptions.InvalidSymbolException;
import com.griddynamics.internship.stonksjh.order.exception.exceptions.OrderNotFoundException;

@RestControllerAdvice(basePackageClasses = OrderCrudController.class)
public class OrderExceptionHandler {
    
    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiExceptionDTO handleOrderNotFoundException(Exception e) {
        return new ApiExceptionDTO(e.getMessage());
    }

    @ExceptionHandler(InvalidStockAmountException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiExceptionDTO handleInvalidStockAmountException(Exception e) {
        return new ApiExceptionDTO(e.getMessage());
    }

    @ExceptionHandler(NoSuchMethodException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiExceptionDTO handleNoSuchMethodException(Exception e) {
        return new ApiExceptionDTO(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiExceptionDTO handleIllegalArgumentException(Exception e) {
        return new ApiExceptionDTO(e.getMessage());
    }

    @ExceptionHandler(InvalidSymbolException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiExceptionDTO handleInvalidSymbolException(Exception e) {
        return new ApiExceptionDTO(e.getMessage());
    }

}
