package com.griddynamics.internship.stonksjh.user.exception;

import com.griddynamics.internship.stonksjh.user.controller.UserCrudController;
import com.griddynamics.internship.stonksjh.user.exception.exceptions.ConflictException;
import com.griddynamics.internship.stonksjh.user.exception.exceptions.DataFormatException;
import com.griddynamics.internship.stonksjh.user.exception.exceptions.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice(basePackageClasses = UserCrudController.class)
@Slf4j
public class UserExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiExceptionDTO handleNotFoundExceptions(Exception e) {
        return ApiExceptionDTO.of(e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiExceptionDTO handleConflictExceptions(Exception e) {
        return ApiExceptionDTO.of(e.getMessage());
    }

    @ExceptionHandler(DataFormatException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiExceptionDTO handleBadRequestExceptions(Exception e) {
        return ApiExceptionDTO.of(e.getMessage());
    }

    @ExceptionHandler(NoSuchMethodException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiExceptionDTO handleInternalServerErrorExceptions(Exception e) {
        return ApiExceptionDTO.of(e.getMessage());
    }

}
