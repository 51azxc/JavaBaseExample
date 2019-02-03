package com.example.spring.boot.security.jwt;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.example.spring.boot.security.jwt.dto.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)       //按方法名顺序来执行测试
@AutoConfigureMockMvc
@Transactional
public class ControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

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

    @Test
    public void testLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user", "user");
        String json = mapper.writeValueAsString(loginRequest);
        mvc.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(content().string("success"));
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
        mvc.perform(get("/resource/user")).andExpect(status().isOk())
                .andExpect(content().string("Hello User user"));
    }

}
