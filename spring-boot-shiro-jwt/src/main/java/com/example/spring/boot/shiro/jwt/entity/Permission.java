package com.example.spring.boot.shiro.jwt.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {
    private Long id;
    private Long parentId;
    private String name;
    private String type;
    private String permission;
    private String url;
}
