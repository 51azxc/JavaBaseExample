package com.example.spring.boot.security.jwt.config;

import com.example.spring.boot.security.jwt.service.JwtTokenProvider;
import com.example.spring.boot.security.jwt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
        String authHeader = "Authorization";
        String authPrefix = "Bearer ";
        Optional.ofNullable(request.getHeader(authHeader))
                .filter(s -> s.startsWith(authPrefix))
                .map(s -> {
                    String token = s.replace(authPrefix, "");
                    return tokenProvider.decode(token);
                })
                .ifPresent(s -> {
                    UserDetails userDetails = userService.loadUserByUsername(s);
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null,
                                    userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                });
        chain.doFilter(request, response);
    }
}
