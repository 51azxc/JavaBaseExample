package com.example.spring.boot.webflux;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.example.spring.boot.webflux.domain.User;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProducerTest {

	@Autowired
	private WebTestClient webTestClient;
	
	
	@Test
	public void test_1_AddUser() {
		User u = new User();
		u.setUsername("a");
		webTestClient.post().uri("/api/users").body(BodyInserters.fromObject(u)).exchange()
			.expectStatus().isOk();
	}
	
	@Test
	public void test_2_ListUsers() {
		webTestClient.get().uri("/api/users").accept(MediaType.APPLICATION_JSON).exchange()
			.expectStatus().isOk()
			.expectBodyList(User.class).hasSize(1);
	}
	
	@Test
	public void test_3_UpdateUser() {
		User u = new User(1, "b");
		webTestClient.put().uri("/api/users/1").body(BodyInserters.fromObject(u)).exchange()
			.expectStatus().isOk();
	}
	
	@Test
	public void test_4_GetUser() {
		webTestClient.get().uri("/api/users/1").accept(MediaType.APPLICATION_JSON).exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody().jsonPath("$.username").isEqualTo("b");
	}
	
	@Test 
	public void test_5_DeleteUser() {
		webTestClient.delete().uri("/api/users/1").accept(MediaType.APPLICATION_JSON).exchange()
			.expectStatus().isOk();
	}
	
}
