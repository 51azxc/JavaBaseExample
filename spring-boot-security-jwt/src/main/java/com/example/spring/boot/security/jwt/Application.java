package com.example.spring.boot.security.jwt;

import com.example.spring.boot.security.jwt.domain.RoleType;
import com.example.spring.boot.security.jwt.domain.User;
import com.example.spring.boot.security.jwt.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.TimeZone;

@SpringBootApplication
//开启jpa auditing
@EnableJpaAuditing
public class Application {
    // 指定使用时间规范
    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        UserService userService = context.getBean(UserService.class);
        if (!userService.existsUser("admin")) {
            User user = new User();
            user.setUsername("admin");
            user.setPassword("admin");
            userService.saveUser(user, Arrays.asList(RoleType.ROLE_ADMIN, RoleType.ROLE_USER));
        }
    }
}
