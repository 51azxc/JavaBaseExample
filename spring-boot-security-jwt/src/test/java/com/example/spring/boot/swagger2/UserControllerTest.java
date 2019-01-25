package com.example.spring.boot.swagger2;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.spring.boot.swagger2.web.UserController;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserControllerTest {
	
	private MockMvc mvc;

	@Before
	public void setUp() {
		mvc = MockMvcBuilders.standaloneSetup(new UserController()).build();
	}

	@Test
	public void test1_addUser() throws Exception {
		mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"a\",\"age\":14}"))
            .andExpect(jsonPath("$.id", is(1)));
	}

    @Test
    public void test2_getUsers() throws Exception {
        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andDo(print());
    }

    @Test
    public void test3_updateUser() throws Exception {
        mvc.perform(put("/users/1").param("name", "a").param("age", "12"))
                .andExpect(jsonPath("$.name", is("a")));
    }

    @Test
    public void test4_getUser() throws Exception {
        String responseBody = mvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andDo(print()).andReturn().getResponse().getContentAsString();
        System.out.println(responseBody);
    }

    @Test
    public void test5_deleteUser() throws Exception {
        mvc.perform(delete("/users/1"))
                .andExpect(content().string(equalTo("1")));
    }

}
