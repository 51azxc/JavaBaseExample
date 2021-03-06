package com.example.spring.boot.webflux.security.jwt.service;

import com.example.spring.boot.webflux.security.jwt.domain.Role;
import com.example.spring.boot.webflux.security.jwt.domain.RoleType;
import com.example.spring.boot.webflux.security.jwt.domain.User;
import com.example.spring.boot.webflux.security.jwt.repository.RoleRepository;
import com.example.spring.boot.webflux.security.jwt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class UserService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        Set<Role> roles = roleRepository.findByUsers_username(username);
        Optional<User> user = userRepository.findByUsername(username);
        user.ifPresent(u -> u.setRoles(roles));
        return Mono.justOrEmpty(user);
    }

    @Transactional
    public User saveUser(User user, List<String> roleTypes) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        List<RoleType> roleTypeList = roleTypes.stream().map(RoleType::fromString).collect(Collectors.toList());
        Set<Role> existRoles = roleRepository.findByRoleTypeIn(roleTypeList);
        if (existRoles.size() != roleTypes.size()) {
            Set<RoleType> existRoleTypes = existRoles.stream().map(Role::getRoleType).collect(Collectors.toSet());
            List<Role> notExistRoles = roleTypeList.stream()
                    .filter(r -> !existRoleTypes.contains(r)).map(Role::new).collect(Collectors.toList());
            existRoles.addAll(roleRepository.saveAll(notExistRoles));
        }
        user.setRoles(existRoles);
        return userRepository.save(user);
    }

    @Transactional
    public void saveRoles(List<String> roleTypes) {
        Set<Role> roles = roleTypes.stream()
                .map(RoleType::fromString)
                .map(Role::new)
                .collect(Collectors.toSet());
        roleRepository.saveAll(roles);
    }

    public boolean existsUser(String username) {
        return userRepository.existsByUsername(username);
    }

    public Mono<String> getUsernameById(Long id) {
        return Mono.justOrEmpty(userRepository.findUsernameById(id));
    }
}
