package com.example.spring.boot.webflux.security.jwt.web;

import com.example.spring.boot.webflux.security.jwt.domain.User;
import com.example.spring.boot.webflux.security.jwt.dto.AuthRequest;
import com.example.spring.boot.webflux.security.jwt.dto.AuthResponse;
import com.example.spring.boot.webflux.security.jwt.exception.UserExistsException;
import com.example.spring.boot.webflux.security.jwt.service.TokenAuthenticationManager;
import com.example.spring.boot.webflux.security.jwt.service.TokenProvider;
import com.example.spring.boot.webflux.security.jwt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.validation.Validator;
import java.util.Arrays;
import java.util.Objects;

@Service
public class UserHandler {

    private final UserService userService;
    private final TokenAuthenticationManager tokenAuthenticationManager;
    private final Validator validator;
    private final TokenProvider tokenProvider;

    @Autowired
    public UserHandler(UserService userService,
                       TokenAuthenticationManager tokenAuthenticationManager,
                       Validator validator, TokenProvider tokenProvider) {
        this.userService = userService;
        this.tokenAuthenticationManager = tokenAuthenticationManager;
        this.validator = validator;
        this.tokenProvider = tokenProvider;
    }

    public Mono<ServerResponse> signIn(ServerRequest request) {
        return request.bodyToMono(AuthRequest.class)
                .filter(authRequest -> validator.validate(authRequest).isEmpty())
                .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid username or password")))
                .flatMap(authRequest -> {
                    Authentication authentication= new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(), authRequest.getPassword());
                    return tokenAuthenticationManager.authenticate(authentication);
                })
                .doOnError(e -> new BadCredentialsException("Invalid username or password"))
                .doOnNext(authentication -> ReactiveSecurityContextHolder.withAuthentication(authentication))
                .map(auth -> new AuthResponse(tokenProvider.encode(auth)))
                .flatMap(authResponse ->
                        ServerResponse.ok().contentType(MediaType.APPLICATION_JSON_UTF8)
                                .body(BodyInserters.fromObject(authResponse))
                                .switchIfEmpty(ServerResponse.badRequest().build()));

    }

    public Mono<ServerResponse> signUp(ServerRequest request) {
        return request.bodyToMono(AuthRequest.class)
                .filter(authRequest -> validator.validate(authRequest).isEmpty())
                .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid username or password")))
                .filter(authRequest -> !userService.existsUser(authRequest.getUsername()))
                .switchIfEmpty(Mono.error(new UserExistsException("Username Exists")))
                .map(authRequest -> new User(authRequest.getUsername(), authRequest.getPassword()))
                .doOnNext(user -> userService.saveUser(user, Arrays.asList("ROLE_USER")))
                .flatMap(user -> ServerResponse.ok()
                        .body(Mono.just("success"), String.class)
                        .switchIfEmpty(ServerResponse.badRequest()
                                .body(Mono.just("failed"), String.class)));
    }

    public Mono<ServerResponse> helloPage(ServerRequest request) {
        return ServerResponse.ok().body(Mono.just("Hello World!"), String.class);
    }

    public Mono<ServerResponse> adminPage(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(this::getCurrentUsername)
                .flatMap(username -> ServerResponse.ok()
                        .body(BodyInserters.fromObject("Hello admin: " + username)));
    }

    public Mono<ServerResponse> userPage(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(this::getCurrentUsername)
                .flatMap(username -> ServerResponse.ok()
                        .body(Mono.just("Hello user: " + username), String.class));
    }

    private Mono<String> getCurrentUsername(SecurityContext securityContext) {
        return Mono.justOrEmpty(securityContext.getAuthentication()).filter(Objects::nonNull)
                .map(authentication -> authentication.getPrincipal()).filter(Objects::nonNull)
                .map(o -> Long.valueOf(o.toString()))
                .flatMap(userService::getUsernameById)
                .switchIfEmpty(Mono.error(new BadCredentialsException("Current user not exists")));
    }
}
