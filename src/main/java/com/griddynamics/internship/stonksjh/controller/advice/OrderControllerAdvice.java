package com.griddynamics.internship.stonksjh.controller.advice;

import com.griddynamics.internship.stonksjh.controller.OrderController;
import com.griddynamics.internship.stonksjh.exception.ApiExceptionDTO;
import com.griddynamics.internship.stonksjh.exception.order.InvalidOrderTypeException;
import com.griddynamics.internship.stonksjh.exception.order.InvalidStockAmountException;
import com.griddynamics.internship.stonksjh.exception.order.InvalidSymbolException;
import com.griddynamics.internship.stonksjh.exception.order.OrderNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = OrderController.class)
public class OrderControllerAdvice {

    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiExceptionDTO handleOrderNotFoundException(Exception e) {
        return ApiExceptionDTO.of(e.getMessage());
    }

    @ExceptionHandler(NoSuchMethodException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiExceptionDTO handleNoSuchMethodException(Exception e) {
        return ApiExceptionDTO.of(e.getMessage());
    }

    @ExceptionHandler({
            InvalidStockAmountException.class,
            InvalidSymbolException.class,
            InvalidOrderTypeException.class,
            IllegalArgumentException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiExceptionDTO handleIllegalArgumentException(Exception e) {
        return ApiExceptionDTO.of(e.getMessage());
    }

}
