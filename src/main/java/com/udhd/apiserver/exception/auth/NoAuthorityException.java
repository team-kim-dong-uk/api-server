package com.udhd.apiserver.exception.auth;

public class NoAuthorityException extends RuntimeException{
    public NoAuthorityException(String message) {
        super(message);
    }
    public NoAuthorityException() {
        super("User with given accesstoken has no authority to requested resource");
    }
}
