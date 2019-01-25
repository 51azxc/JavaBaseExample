package com.example.spring.boot.webflux.config;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.spring.boot.webflux.service.UserHandler;

@Configuration
public class Routes {

	@Autowired
	private UserHandler userHandler;
	
	@Bean
	public RouterFunction<ServerResponse> routerFunction() {
		return route(GET("/api/users").and(accept(APPLICATION_JSON)), userHandler::listUsers)
		  .and(route(GET("/api/users/{id}").and(accept(APPLICATION_JSON)), userHandler::getUserById))
		  .and(route(POST("/api/users").and(contentType(APPLICATION_JSON)), userHandler::addUser))
		  .and(route(PUT("/api/users/{id}").and(contentType(APPLICATION_JSON)), userHandler::updateUser))
		  .and(route(DELETE("/api/users/{id}").and(accept(APPLICATION_JSON)), userHandler::deleteUser));
	}
}
