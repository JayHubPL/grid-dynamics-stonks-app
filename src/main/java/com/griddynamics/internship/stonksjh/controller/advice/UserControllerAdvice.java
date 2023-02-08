package com.griddynamics.internship.stonksjh.controller.advice;

import com.griddynamics.internship.stonksjh.controller.UserController;
import com.griddynamics.internship.stonksjh.exception.ApiExceptionDTO;
import com.griddynamics.internship.stonksjh.exception.user.EmailFormatException;
import com.griddynamics.internship.stonksjh.exception.user.EmailTakenException;
import com.griddynamics.internship.stonksjh.exception.user.UserNotFoundException;
import com.griddynamics.internship.stonksjh.exception.user.UsernameFormatException;
import com.griddynamics.internship.stonksjh.exception.user.UsernameTakenException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = UserController.class)
public class UserControllerAdvice {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiExceptionDTO handleUserNotFoundException(Exception e) {
        return ApiExceptionDTO.of(e.getMessage());
    }

    @ExceptionHandler({
            UsernameTakenException.class,
            EmailTakenException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiExceptionDTO handleConflictExceptions(Exception e) {
        return ApiExceptionDTO.of(e.getMessage());
    }

    @ExceptionHandler({
            UsernameFormatException.class,
            EmailFormatException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiExceptionDTO handleDataFormatException(Exception e) {
        return ApiExceptionDTO.of(e.getMessage());
    }

}
