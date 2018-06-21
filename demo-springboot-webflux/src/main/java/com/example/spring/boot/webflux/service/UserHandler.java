package com.example.spring.boot.webflux.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.spring.boot.webflux.domain.User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserHandler {

	@Autowired
	private UserRepository repo;
	@Value("${app.version:v1}")
	private String version;
	
	public Mono<ServerResponse> listUsers(ServerRequest request) {
		Flux<User> users = repo.getUsers();
		return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(users, User.class);
	}
	
	public Mono<ServerResponse> getUserById(ServerRequest request) {
		Integer userId = Integer.valueOf(request.pathVariable("id"));
		Mono<ServerResponse> notFound = ServerResponse.notFound().build();
		Mono<User> user = repo.getUserById(userId);
		return user.flatMap(u -> ServerResponse.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body(BodyInserters.fromObject(u)))
				.switchIfEmpty(notFound);
	}
	
	public Mono<ServerResponse> addUser(ServerRequest request) {
		Mono<User> user = request.bodyToMono(User.class).doOnNext(u->u.setVersion(version));
		return repo.createUser(user)
				.flatMap(id -> ServerResponse.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(BodyInserters.fromObject(id)))
				.switchIfEmpty(ServerResponse.badRequest().build());
	}
	
	public Mono<ServerResponse> updateUser(ServerRequest request) {
		Integer userId = Integer.valueOf(request.pathVariable("id"));
		Mono<User> user = request.bodyToMono(User.class).doOnNext(u->{
			u.setId(userId);
			u.setVersion(version);
		});
		return repo.updateUser(user)
				.flatMap(u -> ServerResponse.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(BodyInserters.fromObject(u)))
				.switchIfEmpty(ServerResponse.badRequest().build());
	}
	
	public Mono<ServerResponse> deleteUser(ServerRequest request) {
		Integer userId = Integer.valueOf(request.pathVariable("id"));
		return repo.removeUser(userId).flatMap(user -> ServerResponse.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(BodyInserters.fromObject(user)))
				.switchIfEmpty(ServerResponse.badRequest().build());
	}
}
