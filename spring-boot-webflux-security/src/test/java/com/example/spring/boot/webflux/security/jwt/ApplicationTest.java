package com.example.spring.boot.webflux.security.jwt;

import com.example.spring.boot.webflux.security.jwt.dto.AuthRequest;
import com.example.spring.boot.webflux.security.jwt.dto.AuthResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "600000")		//定义过期时间
public class ApplicationTest {

    @Autowired WebTestClient webTestClient;

    @Test
    public void testSignIn() {
        AuthRequest request = new AuthRequest("user", "user");
        webTestClient.post().uri("register").body(BodyInserters.fromObject(request))
                .header("Content-Type", "application/json")
                .exchange().expectBody(String.class).isEqualTo("success");

        AuthResponse response = webTestClient.post().uri("login").body(BodyInserters.fromObject(request))
                .header("Content-Type", "application/json")
                .exchange().expectBody(AuthResponse.class)
                .returnResult().getResponseBody();
        System.out.println(response.getToken());

    }
}
