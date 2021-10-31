package com.udhd.apiserver.web;

import com.udhd.apiserver.exception.EntityNotFoundException;
import com.udhd.apiserver.exception.auth.DuplicateNicknameException;
import com.udhd.apiserver.exception.auth.NoAuthorityException;
import com.udhd.apiserver.web.dto.ErrorResponse;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

  @ExceptionHandler(NoAuthorityException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ErrorResponse forbidden(NoAuthorityException e) {
    return new ErrorResponse(HttpStatus.FORBIDDEN.value(), "Resource forbidden",
        Arrays.asList(e.getMessage()));
  }

  @ExceptionHandler(EntityNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorResponse entityNotFound(EntityNotFoundException e) {
    return new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Resource not found",
        Arrays.asList(e.getMessage()));
  }

  @ExceptionHandler(DuplicateNicknameException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ErrorResponse duplicateNickname(DuplicateNicknameException e) {
    return new ErrorResponse(HttpStatus.CONFLICT.value(), "Duplicate nickname",
        Arrays.asList(e.getMessage()));
  }


}
