package com.example.spring.boot.webflux.security.jwt.service;

import com.example.spring.boot.webflux.security.jwt.entity.SystemRole;
import com.example.spring.boot.webflux.security.jwt.entity.SystemUser;
import com.example.spring.boot.webflux.security.jwt.mapper.UserRoleMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SystemUserService {

    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public SystemUserService(UserRoleMapper userRoleMapper, PasswordEncoder passwordEncoder) {
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<SystemUser> findByUsername(String s) {
        return userRoleMapper.findUserByUsername(s);
    }

    @Transactional
    public boolean saveUser(SystemUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRoleMapper.addUser(user);
        assert user.getId() > 0;
        List<SystemRole> roles = new ArrayList<>(user.getRoles());
        userRoleMapper.addRoles(roles);
        List<Long> rids = roles.stream().map(SystemRole::getId).collect(Collectors.toList());
        int result = userRoleMapper.addUserRoles(user.getId(), rids);
        return result == roles.size();
    }
}
