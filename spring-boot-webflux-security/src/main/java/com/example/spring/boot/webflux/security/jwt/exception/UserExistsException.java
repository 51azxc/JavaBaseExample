package com.example.spring.boot.webflux.security.jwt.exception;

public class UserExistsException extends RuntimeException  {

    public UserExistsException(String message) {
        super(message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
