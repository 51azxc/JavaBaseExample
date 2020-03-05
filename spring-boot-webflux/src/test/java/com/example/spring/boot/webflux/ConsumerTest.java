package com.example.spring.boot.webflux;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.example.spring.boot.webflux.domain.User;

/*
 * 需要启动服务，即运行Application.java
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@AutoConfigureWebTestClient(timeout = "600000")		//定义过期时间
public class ConsumerTest {

	@Autowired
	private WebTestClient webTestClient;
	
	@Test
	public void test_1_AddUser() {
		User u = new User();
		u.setUsername("a");
		String s = new String(webTestClient.post().uri("/users").body(BodyInserters.fromObject(u)).exchange()
			.expectStatus().isOk().expectBody().returnResult().getResponseBodyContent());
		System.out.println(s);
	}
	
	@Test
	public void test_2_ListUsers() {
		webTestClient.get().uri("/users").accept(MediaType.APPLICATION_JSON).exchange()
			.expectStatus().isOk()
			.expectBodyList(User.class).hasSize(1);
	}
	
	@Test
	public void test_3_UpdateUser() {
		User u = new User(1, "b");
		String s = new String(webTestClient.put().uri("/users/1").body(BodyInserters.fromObject(u)).exchange()
			.expectStatus().isOk().expectBody().returnResult().getResponseBodyContent());
		System.out.println(s);
	}
	
	@Test
	public void test_4_GetUser() {
		String s = new String(webTestClient.get().uri("/users/1").accept(MediaType.APPLICATION_JSON).exchange()
			.expectStatus().isOk()
			.expectBody().returnResult().getResponseBodyContent());
		System.out.println(s);
	}
	
	@Test 
	public void test_5_DeleteUser() {
		String s = new String(webTestClient.delete().uri("/users/1").accept(MediaType.APPLICATION_JSON).exchange()
			.expectStatus().isOk().expectBody().returnResult().getResponseBodyContent());
		System.out.println(s);
	}
	
}
