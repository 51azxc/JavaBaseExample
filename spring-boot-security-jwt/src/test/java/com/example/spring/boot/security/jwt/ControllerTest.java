package com.example.spring.boot.security.jwt;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.example.spring.boot.security.jwt.domain.RoleType;
import com.example.spring.boot.security.jwt.domain.User;
import com.example.spring.boot.security.jwt.dto.LoginRequest;
import com.example.spring.boot.security.jwt.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)       //按方法名顺序来执行测试
@AutoConfigureMockMvc
@Transactional
public class ControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UserService userService;
/*
    @Before
    public void setUp() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("admin");
        userService.saveUser(admin, Arrays.asList(RoleType.ROLE_ADMIN, RoleType.ROLE_USER));

        User user = new User();
        user.setUsername("user");
        user.setPassword("user");
        userService.saveUser(user, Arrays.asList(RoleType.ROLE_USER));
    }

    @Test
    public void testAnonymous() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk())
                .andExpect(content().string("Hello World")).andDo(print());
    }
*/
    @Test
    public void testNoAuth() throws Exception {
        mvc.perform(get("/resource/user")).andExpect(status().isUnauthorized());
    }

    @Test
    public void testRegister() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin", "admin");
        String json = mapper.writeValueAsString(loginRequest);
        mvc.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(content().string("success"));
    }

    @WithUserDetails(userDetailsServiceBeanName = "userService")
    @Test
    public void testLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user", "user");
        String json = mapper.writeValueAsString(loginRequest);
        mvc.perform(post("/users/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(jsonPath("$.type", equalTo("Bearer")));
    }

    @WithMockUser(username = "admin", roles = {"ADMIN","USER"})
    @Test
    public void testAdminAccess() throws Exception {
        String responseBody = mvc.perform(get("/resource/admin")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseBody);
    }

    @WithMockUser(username = "user")
    @Test
    public void testUserAccess() throws Exception {
        mvc.perform(get("/resource/admin")).andExpect(status().is5xxServerError());
        mvc.perform(get("/users/me")).andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("user")));
    }
}
