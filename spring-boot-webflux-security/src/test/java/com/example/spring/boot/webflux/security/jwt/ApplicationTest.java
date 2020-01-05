package com.example.spring.boot.webflux.security.jwt;

import com.example.spring.boot.webflux.security.jwt.domain.RoleType;
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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTest {

    @Autowired SystemUserService systemUserService;

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
}
