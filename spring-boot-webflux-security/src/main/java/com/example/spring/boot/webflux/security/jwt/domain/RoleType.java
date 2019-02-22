package com.example.spring.boot.webflux.security.jwt.domain;

public enum RoleType {
    ROLE_ADMIN,
    ROLE_USER;

    public static RoleType fromString(String type) {
        if ("ROLE_ADMIN".equals(type)) {
            return ROLE_ADMIN;
        } else if ("ROLE_USER".equals(type)) {
            return ROLE_USER;
        } else {
            return null;
        }
    }
}
