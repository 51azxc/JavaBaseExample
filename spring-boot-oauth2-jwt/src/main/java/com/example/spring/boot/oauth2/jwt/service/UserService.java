package com.example.spring.boot.oauth2.jwt.service;

import com.example.spring.boot.oauth2.jwt.domain.Role;
import com.example.spring.boot.oauth2.jwt.domain.RoleType;
import com.example.spring.boot.oauth2.jwt.domain.User;
import com.example.spring.boot.oauth2.jwt.repository.RoleRepository;
import com.example.spring.boot.oauth2.jwt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        return userRepository.findByUsername(s)
                .orElseThrow(() -> new UsernameNotFoundException("user " + s + " not found"));
    }

    @Transactional
    public User saveUser(User user, List<RoleType> roleTypes) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Set<Role> roles = roleTypes.stream().map(roleType -> roleRepository.findByRoleType(roleType).get())
                .collect(Collectors.toSet());
        user.setRoles(roles);
        return userRepository.save(user);
    }

    @Transactional
    public void saveRoles(List<RoleType> roleTypes) {
        Set<Role> roles = roleTypes.stream()
                .map(roleType -> {
                    Role role = new Role();
                    role.setRoleType(roleType);
                    return role;
                })
                .collect(Collectors.toSet());
        roleRepository.saveAll(roles);
    }

}
