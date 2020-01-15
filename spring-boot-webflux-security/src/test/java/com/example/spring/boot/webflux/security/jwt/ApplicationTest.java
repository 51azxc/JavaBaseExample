package com.example.spring.boot.webflux.security.jwt;

import com.example.spring.boot.webflux.security.jwt.domain.RoleType;
import com.example.spring.boot.webflux.security.jwt.dto.AuthRequest;
import com.example.spring.boot.webflux.security.jwt.dto.AuthResponse;
import com.example.spring.boot.webflux.security.jwt.entity.SystemRole;
import com.example.spring.boot.webflux.security.jwt.entity.SystemUser;
import com.example.spring.boot.webflux.security.jwt.service.SystemUserService;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationTest {

    @Autowired SystemUserService systemUserService;
    @Autowired WebTestClient webTestClient;

    @Test
    public void testUser() {
        SystemUser user = new SystemUser();
        user.setUsername("a");
        user.setPassword("a");
        Set<SystemRole> roles = new HashSet<>(2);
        roles.add(new SystemRole(RoleType.ROLE_ADMIN.name()));
        roles.add(new SystemRole(RoleType.ROLE_USER.name()));
        user.setRoles(roles);
        Assert.assertTrue(systemUserService.saveUser(user));
        Optional<SystemUser> optional = systemUserService.findByUsername("a");
        Assert.assertTrue(optional.isPresent());
        Set<String> roleSet = optional.get().getRoles().stream().map(SystemRole::getRoleType).collect(Collectors.toSet());
        Assert.assertThat(roleSet, CoreMatchers.hasItems(RoleType.ROLE_ADMIN.name(), RoleType.ROLE_USER.name()));
    }

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
