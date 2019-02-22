package com.example.spring.boot.webflux.security.jwt.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import org.springframework.security.access.AccessDeniedException;
import reactor.core.publisher.Mono;

@Component
@Order(-2)  // Spring自带的WebHandler为-1
public class GlobalExceptionHandler implements WebExceptionHandler {

    private final static Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        logger.error(ex.getLocalizedMessage(), ex);

        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpStatus status;
        String message;
        if (ex instanceof WebExchangeBindException) {
            status = HttpStatus.UNPROCESSABLE_ENTITY;
            WebExchangeBindException e = (WebExchangeBindException) ex;
            StringBuilder sb = new StringBuilder();
            e.getBindingResult().getFieldErrors().forEach(error ->
                    sb.append(error.getField() + ": " + error.getDefaultMessage() + "\n"));
            message = sb.toString();
        } else if (ex instanceof ResponseStatusException) {
            ResponseStatusException e = (ResponseStatusException)ex;
            status = e.getStatus();
            message = e.getReason();
        } else {
            message = ex.getLocalizedMessage();
            if (ex instanceof BadCredentialsException || ex instanceof AuthenticationException) {
                status = HttpStatus.UNAUTHORIZED;
            } else if (ex instanceof UserExistsException) {
                status = HttpStatus.BAD_REQUEST;
            }else if (ex instanceof UsernameNotFoundException) {
                status = HttpStatus.NOT_FOUND;
            } else if (ex instanceof AccessDeniedException) {
                status = HttpStatus.FORBIDDEN;
            } else {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }
        exchange.getResponse().setStatusCode(status);
        ApiError error = new ApiError(status, message);
        Mono mono;
        try {
            DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap(objectMapper.writeValueAsBytes(error));
            mono = Mono.just(dataBuffer);
        } catch (JsonProcessingException e) {
            logger.warn(e.getMessage());
            mono = Mono.empty();
        }
        return exchange.getResponse().writeWith(mono);
    }
}
