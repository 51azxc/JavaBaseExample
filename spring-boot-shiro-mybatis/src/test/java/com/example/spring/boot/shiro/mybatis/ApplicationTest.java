package com.example.spring.boot.shiro.mybatis;

import com.example.spring.boot.shiro.mybatis.entity.Role;
import com.example.spring.boot.shiro.mybatis.entity.User;
import com.example.spring.boot.shiro.mybatis.service.UserService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class ApplicationTest {
    @Autowired UserService userService;

    @Test
    public void testAddUser() {
        Role adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setRoleName("ROLE_ADMIN");
        Role userRole = new Role();
        userRole.setId(2L);
        userRole.setRoleName("ROLE_USER");

        User user = new User();
        user.setUsername("user1");
        user.setPassword("user");
        user.setRoles(Collections.singletonList(userRole));
        User u1 = userService.addUser(user);
        Assert.assertTrue(u1.getId() > 0);

        User admin = new User();
        admin.setUsername("admin1");
        admin.setPassword("admin");
        admin.setRoles(Arrays.asList(adminRole, userRole));
        User u2 = userService.addUser(admin);
        Assert.assertTrue(u2.getId() > 0);
    }

    @Test
    public void testGetUsers() {
        List<User> list = userService.getAllUsers();
        Assert.assertTrue(list.size() == 2);
        Optional<User> optional = userService.getUserByUsername("admin");
        Assert.assertTrue(optional.isPresent());
    }
}
