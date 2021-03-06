package com.example.spring.boot.webflux.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import reactor.netty.http.server.HttpServer;


@Configuration
public class HttpServerConfig {
	
	@Autowired
	private Environment environment;
	
	@Bean
	public HttpServer httpServer(RouterFunction<?> routerFunction) {
		HttpHandler handler = RouterFunctions.toHttpHandler(routerFunction);
		ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(handler);
		HttpServer server = HttpServer.create();
		server.host("localhost");
		server.port(Integer.valueOf(environment.getProperty("server.port")));
		//server.newHandler(adapter);
		return server;
	}
}
