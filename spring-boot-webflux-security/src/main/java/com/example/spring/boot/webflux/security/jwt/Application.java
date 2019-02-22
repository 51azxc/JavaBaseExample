package com.example.spring.boot.webflux.security.jwt;

import com.example.spring.boot.webflux.security.jwt.domain.User;
import com.example.spring.boot.webflux.security.jwt.repository.RoleRepository;
import com.example.spring.boot.webflux.security.jwt.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

@SpringBootApplication
public class Application {
    @PostConstruct void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        UserService userService = context.getBean(UserService.class);
        List<String> roles = Arrays.asList("ROLE_ADMIN", "ROLE_USER");
        userService.saveRoles(roles);
        userService.saveUser(new User("admin", "admin"), roles);
    }
}
