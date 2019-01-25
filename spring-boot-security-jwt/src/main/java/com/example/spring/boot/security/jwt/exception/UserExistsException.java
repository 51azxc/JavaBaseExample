package com.example.spring.boot.security.jwt.exception;

public class UserExistsException extends RuntimeException  {

    public UserExistsException(String message) {
        super(message);
    }

    //不会输出trace
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
