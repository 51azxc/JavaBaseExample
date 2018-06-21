package com.example.spring.boot.webflux.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.spring.boot.webflux.domain.User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("users")
public class UserController {

	@Autowired
	private WebClient webClient;
	
	@GetMapping
	public Flux<User> list() {
		return webClient.get().uri("/api/users").accept(MediaType.APPLICATION_JSON).retrieve().bodyToFlux(User.class);
	}
	
	@GetMapping("{id}")
	public Mono<String> getUser(@PathVariable final Integer id) {
		return webClient.get().uri("/api/users/{id}",id).accept(MediaType.APPLICATION_JSON)
				.retrieve().onStatus(HttpStatus::is4xxClientError, res -> Mono.error(new Throwable(res.statusCode().getReasonPhrase())))
				.bodyToMono(User.class)
				.flatMap(user->Mono.justOrEmpty("Hello, " + user.getUsername() + " " + user.getVersion()))
				.onErrorReturn("error");
	}
	
	@PostMapping
	public Mono<Integer> addUser(@RequestBody final User user) {
		return webClient.post().uri("/api/users").contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromObject(user)).retrieve()
				.bodyToMono(Integer.class);
	}
	
	@PutMapping("{id}")
	public Mono<User> updateUser(@PathVariable final Integer id, @RequestBody final User user) {
		return webClient.put().uri("/api/users/{id}",id).contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromObject(user)).retrieve()
				.bodyToMono(User.class);
	}
	
	@DeleteMapping("{id}")
	public Mono<String> deleteUser(@PathVariable final Integer id) {
		return webClient.delete().uri("/api/users/{id}",id).accept(MediaType.APPLICATION_JSON)
				.retrieve().bodyToMono(User.class)
				.doOnError(error->Mono.error(error))
				.flatMap(user->Mono.justOrEmpty("Bye, " + user.getUsername() + " " + user.getVersion()))
				.onErrorReturn("error");
	}
}
