package com.example.spring.boot.shiro.jwt.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole {
    private Long userId;
    private Long roleId;
}
