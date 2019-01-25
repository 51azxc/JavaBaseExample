package com.example.spring.boot.security.jwt.exception;

import org.springframework.security.core.AuthenticationException;

public class UnAuthenticationException extends AuthenticationException {

    public UnAuthenticationException(String message) {
        super(message);
    }

    //不会输出trace
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
