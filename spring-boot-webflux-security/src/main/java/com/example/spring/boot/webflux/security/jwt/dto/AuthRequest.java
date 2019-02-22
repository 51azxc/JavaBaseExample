package com.example.spring.boot.webflux.security.jwt.dto;

import javax.validation.constraints.*;

public class AuthRequest {

    @NotBlank @Size(min = 3, max = 20)
    private String username;
    @NotBlank
    private String password;

    public AuthRequest() {
    }

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
