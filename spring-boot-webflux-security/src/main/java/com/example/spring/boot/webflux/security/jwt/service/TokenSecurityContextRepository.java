package com.example.spring.boot.webflux.security.jwt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class TokenSecurityContextRepository implements ServerSecurityContextRepository {

    private final TokenProvider tokenProvider;
    private final TokenAuthenticationManager tokenAuthenticationManager;

    @Autowired
    public TokenSecurityContextRepository(TokenProvider tokenProvider, TokenAuthenticationManager tokenAuthenticationManager) {
        this.tokenProvider = tokenProvider;
        this.tokenAuthenticationManager = tokenAuthenticationManager;
    }

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return Mono.defer(() -> Mono.error(new UnsupportedOperationException("No save method")));
        //throw new UnsupportedOperationException("No save method");
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .filter(s -> s.length() > 7 && s.startsWith("Bearer "))
                .map(s -> tokenProvider.decode(s.substring(7)))
                .onErrorResume(Mono::error)
                .flatMap(auth -> tokenAuthenticationManager.authenticate(auth))
                .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid Credentials")))
                .map(SecurityContextImpl::new);
    }
}
