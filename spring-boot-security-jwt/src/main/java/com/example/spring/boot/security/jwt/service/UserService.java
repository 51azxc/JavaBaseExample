package com.example.spring.boot.security.jwt.service;

import com.example.spring.boot.security.jwt.domain.Role;
import com.example.spring.boot.security.jwt.domain.RoleType;
import com.example.spring.boot.security.jwt.domain.User;
import com.example.spring.boot.security.jwt.repository.RoleRepository;
import com.example.spring.boot.security.jwt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.security.RolesAllowed;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class UserService implements UserDetailsService {

    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(s)
                .orElseThrow(() -> new UsernameNotFoundException("Username is not exists "));
        //if user.fetch = eager
        //Set<Role> roles = user.getRoles();
        //if user.fetch = lazy
        Set<Role> roles = roleRepository.findByUsers_username(s);
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleType().name()))
                .collect(Collectors.toList());
        return new org.springframework.security.core.userdetails.User(user.getUsername(),
                user.getPassword(), authorities);
    }

    @Transactional
    public User saveUser(User user, List<RoleType> roleTypes) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Set<Role> roles = roleTypes.stream().map(roleType -> roleRepository.findByRoleType(roleType).get())
                .collect(Collectors.toSet());
        user.setRoles(roles);
        return userRepository.save(user);
    }

    public boolean existsUser(String username) {
        return userRepository.existsByUsername(username);
    }

    @Secured({"ROLE_USER","ROLE_ADMIN"})
    public Optional<User> getUser(String username) {
        return userRepository.findByUsername(username);
    }

    @RolesAllowed("ROLE_ADMIN")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

}
