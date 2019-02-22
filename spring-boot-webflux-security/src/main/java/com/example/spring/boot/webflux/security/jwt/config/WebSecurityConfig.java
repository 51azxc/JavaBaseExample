package com.example.spring.boot.webflux.security.jwt.config;

import com.example.spring.boot.webflux.security.jwt.repository.RoleRepository;
import com.example.spring.boot.webflux.security.jwt.repository.UserRepository;
import com.example.spring.boot.webflux.security.jwt.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableReactiveMethodSecurity
@EnableWebFluxSecurity
public class WebSecurityConfig {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TokenProvider tokenProvider;

    @Autowired
    public WebSecurityConfig(UserRepository userRepository,
                             RoleRepository roleRepository,
                             TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.tokenProvider = tokenProvider;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity) {
        return httpSecurity
                .csrf().disable()
                .logout().disable()
                .cors()
                .and()
                .exceptionHandling()
                .accessDeniedHandler((exchange, e) -> Mono.error(e))
                .and()
                .authenticationManager(tokenAuthenticationManager())
                .securityContextRepository(tokenSecurityContextRepository())
                .authorizeExchange()
                .pathMatchers("/login","/register","/favicon.ico").permitAll()
                .pathMatchers(HttpMethod.GET, "/").permitAll()
                .pathMatchers("/admin").hasRole("ADMIN")
                .pathMatchers("/user").hasRole("USER")
                .anyExchange().authenticated()
                .and()
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserService userService() {
        return new UserService(userRepository, roleRepository, passwordEncoder());
    }

    @Bean
    public TokenAuthenticationManager tokenAuthenticationManager() {
        return new TokenAuthenticationManager(userService(), passwordEncoder());
    }

    @Bean
    public TokenSecurityContextRepository tokenSecurityContextRepository() {
        return new TokenSecurityContextRepository(tokenProvider, tokenAuthenticationManager());
    }
}
