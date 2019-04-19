package com.example.spring.boot.oauth2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;

@SpringBootApplication
@RestController
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @GetMapping("/users/me")
    public HttpEntity user(Principal principal) {
        return ResponseEntity.ok(principal);
    }

    @GetMapping("/test")
    public String test(@RequestParam(required = false) String code) {
        if (code != null) {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
            map.add("grant_type", "authorization_code");
            map.add("code", code);
            map.add("client_id", "client");
            map.add("client_secret", "client");
            map.add("redirect_uri", "http://localhost:8080/test");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            String url = "http://localhost:8080/oauth/token";
            ResponseEntity<String> response = restTemplate.postForEntity( url, request , String.class );
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            return code;
        }
        return "test";
    }
}
