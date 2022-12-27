package com.griddynamics.internship.stonksjh.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.griddynamics.internship.stonksjh.order.controller.OrderCrudController;
import com.griddynamics.internship.stonksjh.order.exception.exceptions.InvalidStockAmountException;
import com.griddynamics.internship.stonksjh.order.exception.exceptions.OrderNotFoundException;

import lombok.val;

@ControllerAdvice(basePackageClasses = OrderCrudController.class)
public class OrderExceptionHandler {
    
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiExceptionDTO> handleOrderNotFoundException(Exception e) {
        val apiExceptionDTO = new ApiExceptionDTO(e.getMessage());
        return new ResponseEntity<>(apiExceptionDTO, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidStockAmountException.class)
    public ResponseEntity<ApiExceptionDTO> handleInvalidStockAmountException(Exception e) {
        val apiExceptionDTO = new ApiExceptionDTO(e.getMessage());
        return new ResponseEntity<>(apiExceptionDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoSuchMethodException.class)
    public ResponseEntity<ApiExceptionDTO> handleNoSuchMethodException(Exception e) {
        val apiExceptionDTO = new ApiExceptionDTO(e.getMessage());
        return new ResponseEntity<>(apiExceptionDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
