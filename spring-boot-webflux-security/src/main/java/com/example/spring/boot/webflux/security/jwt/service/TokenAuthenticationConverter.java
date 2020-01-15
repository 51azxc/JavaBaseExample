package com.example.spring.boot.webflux.security.jwt.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

public class TokenAuthenticationConverter implements ServerAuthenticationConverter {
    private final static String BEARER = "Bearer ";

    private final TokenProvider tokenProvider;

    public TokenAuthenticationConverter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange).map(ServerWebExchange::getRequest).map(ServerHttpRequest::getHeaders)
                .filter(Objects::nonNull).map(h -> h.getFirst(HttpHeaders.AUTHORIZATION))
                .filter(a -> a.length() > BEARER.length()).map(t -> t.substring(BEARER.length()))
                .map(tokenProvider::decode).filter(Objects::nonNull);
    }
}
