package com.example.spring.boot.webflux.security.jwt.service;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;

import java.net.URI;

public class TokenAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> mono, AuthorizationContext authorizationContext) {
        final String method = authorizationContext.getExchange().getRequest().getMethod().name();
        URI uri = authorizationContext.getExchange().getRequest().getURI();
        System.out.println(method + " " + uri.getPath());
        String path = uri.getPath().replaceAll("/","");
        final String role = "ROLE_" + path.toUpperCase();
        return mono.filter(Authentication::isAuthenticated)
                .map(auth -> auth.getAuthorities().stream().filter(g -> g.getAuthority().equals(role)).count() > 0)
            .defaultIfEmpty(false)
            .map(AuthorizationDecision::new);
    }
}
