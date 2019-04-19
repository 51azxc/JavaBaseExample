package com.example.spring.boot.oauth2.jwt;

import com.example.spring.boot.oauth2.jwt.domain.Role;
import com.example.spring.boot.oauth2.jwt.domain.RoleType;
import com.example.spring.boot.oauth2.jwt.domain.User;
import com.example.spring.boot.oauth2.jwt.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@RestController
public class Application {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        UserService userService = context.getBean(UserService.class);
        List<RoleType> roles = Arrays.asList(RoleType.ROLE_ADMIN, RoleType.ROLE_USER);
        userService.saveRoles(roles);
        User user = new User();
        user.setUsername("admin");
        user.setPassword("admin");
        userService.saveUser(user, roles);
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
            map.add("client_id", "client2");
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
