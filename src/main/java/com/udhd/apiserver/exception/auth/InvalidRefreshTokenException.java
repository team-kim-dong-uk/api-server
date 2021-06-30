package com.udhd.apiserver.exception.auth;


public class InvalidRefreshTokenException extends RuntimeException{
    public InvalidRefreshTokenException(String message) {
        super(message);
    }

}
