package com.udhd.apiserver.web;

import com.udhd.apiserver.web.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class AppRestControllerAdvice {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse methodArgumentNotValid(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getAllErrors()
                                        .stream().map((error) ->
                                            ((FieldError) error).getField() + " " + error.getDefaultMessage()
                                        ).collect(Collectors.toList());
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Method argument not valid", errors);
    }
}
