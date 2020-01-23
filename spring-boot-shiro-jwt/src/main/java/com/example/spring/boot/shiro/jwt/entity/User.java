package com.example.spring.boot.shiro.jwt.entity;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Long id;
    private String username;
    private String password;
    private String salt;
    private Set<Role> roles;
}
