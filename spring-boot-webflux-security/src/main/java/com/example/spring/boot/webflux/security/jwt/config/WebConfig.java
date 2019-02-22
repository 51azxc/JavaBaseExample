package com.example.spring.boot.webflux.security.jwt.config;

import com.example.spring.boot.webflux.security.jwt.web.UserHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {

    private final UserHandler userHandler;

    public WebConfig(UserHandler userHandler) {
        this.userHandler = userHandler;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedHeaders("*").allowedOrigins("*")
                .allowedMethods("HEAD", "OPTIONS", "GET", "POST", "PUT", "PATCH", "DELETE")
                .maxAge(3600);
    }

    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        return RouterFunctions.route(GET("/"), userHandler::helloPage)
                .andRoute(POST("/login")
                        .and(contentType(MediaType.APPLICATION_JSON)
                        .and(accept(MediaType.APPLICATION_JSON))), userHandler::signIn)
                .andRoute(POST("/register")
                        .and(contentType(MediaType.APPLICATION_JSON)), userHandler::signUp)
                .andRoute(GET("/admin"), userHandler::adminPage)
                .andRoute(GET("/user"), userHandler::userPage);
    }
}
