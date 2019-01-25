package com.example.spring.boot.webflux.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

//提供一些UserController需要的依赖
@Configuration
public class AppConfig {
	
	@Value("${service.url}")
	private String serviceUrl;
	
	@Bean WebClient webClient() {
		return WebClient.create(serviceUrl);
	}
}
