package com.example.spring.boot.webflux.security.jwt.service;

import com.example.spring.boot.webflux.security.jwt.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

public class TokenAuthenticationManager implements ReactiveAuthenticationManager {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Autowired
    public TokenAuthenticationManager(UserService userService, PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        if (authentication.isAuthenticated()) { return Mono.just(authentication); }
        return Mono.just(authentication)
                .switchIfEmpty(Mono.error(new BadCredentialsException("Bad Credentials")))
                //.cast(UsernamePasswordAuthenticationToken.class)
                .map(authenticationToken -> authenticationToken.getPrincipal().toString())
                //.filter(token -> SecurityContextHolder.getContext().getAuthentication() == null)
                .flatMap(userService::findByUsername)
                //.flatMap(token -> authenticateUser(token))
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found")))
                .filter(u -> passwordEncoder.matches(authentication.getCredentials().toString(), u.getPassword()))
                .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid username or password")))
                .cast(User.class)
                .map(u -> new UsernamePasswordAuthenticationToken(u.getId(), null, u.getAuthorities()));
    }
/*
    private Mono<UserDetails> authenticateUser(Authentication authentication) {
        String username = authentication.getPrincipal().toString();
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            return userService.findByUsername(username);
        }
        return Mono.empty();
    }
*/
}
