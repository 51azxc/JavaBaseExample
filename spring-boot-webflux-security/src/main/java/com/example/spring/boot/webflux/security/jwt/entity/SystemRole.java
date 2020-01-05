package com.example.spring.boot.webflux.security.jwt.entity;

public class SystemRole {
    private Long id;
    private String roleType;

    public SystemRole() {
    }

    public SystemRole(String roleType) {
        this.roleType = roleType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }
}
