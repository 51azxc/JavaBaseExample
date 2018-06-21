package com.example.spring.boot.webflux.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Repository;

import com.example.spring.boot.webflux.domain.User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class UserRepository {
	private Map<Integer, User> map = new ConcurrentHashMap<>();
	private final static AtomicInteger ids = new AtomicInteger(0);
	
	public Mono<User> getUserById(Integer id) {
		return Mono.justOrEmpty(map.get(id)).switchIfEmpty(Mono.empty());
	}
	
	public Flux<User> getUsers() {
		return Flux.fromIterable(map.values());
	}
	
	public Mono<Integer> createUser(final Mono<User> user) {
		return user.doOnNext(u -> {
			//Integer id = (map.isEmpty()?0:map.keySet().stream().max(Comparator.comparing(Integer::valueOf)).get())+1;
			Integer id = ids.incrementAndGet();
			u.setId(id);
			map.put(id, u);
		}).flatMap(u -> Mono.just(u.getId()));
	}
	
	public Mono<User> updateUser(final Mono<User> user) {
		return user.doOnNext(u -> map.put(u.getId(), u));
	}
	
	public Mono<User> removeUser(final Integer id) {
		return Mono.justOrEmpty(map.remove(id)).switchIfEmpty(Mono.empty());
	}
}
