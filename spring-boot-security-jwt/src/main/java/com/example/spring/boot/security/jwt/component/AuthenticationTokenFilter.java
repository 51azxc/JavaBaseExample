package com.example.spring.boot.security.jwt.component;

import com.example.spring.boot.security.jwt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
public class AuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired UserService userService;
    @Autowired JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        String authPrefix = "Bearer ";
        Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .filter(s -> s.startsWith(authPrefix))
                .flatMap(s -> {
                    String token = s.replace(authPrefix, "");
                    Long id = tokenProvider.decode(token);
                    return userService.getUserPrincipalById(id);
                })
                .ifPresent(userPrincipal -> {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userPrincipal, null,
                                    userPrincipal.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                });
        chain.doFilter(request, response);
    }
}
