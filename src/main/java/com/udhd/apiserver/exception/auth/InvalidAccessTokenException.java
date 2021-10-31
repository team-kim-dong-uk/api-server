package com.udhd.apiserver.exception.auth;

public class InvalidAccessTokenException extends RuntimeException {

  public InvalidAccessTokenException(String message) {
    super(message);
  }
}
