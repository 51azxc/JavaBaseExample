package com.example.spring.boot.shiro.mybatis.web;

import com.example.spring.boot.shiro.mybatis.entity.User;
import com.example.spring.boot.shiro.mybatis.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class IndexController {

    private final UserService userService;

    public IndexController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String index() {
        return "Hello";
    }

    @GetMapping("/login")
    public String login() { return "Please login"; }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(new UsernamePasswordToken(username, password));
            return "success";
        } catch (AuthenticationException e) {
            e.printStackTrace();
            return "failed";
        }
    }

    @GetMapping("/logout")
    public String logout() {
        SecurityUtils.getSubject().logout();
        return "logout";
    }

    @GetMapping("/403")
    public HttpEntity unauthorizedPage() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
    }

    @GetMapping("/users")
    public List<User> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/admin")
    public String adminPage() {
        return "admin page";
    }

    @RequiresRoles("ROLE_USER")
    @GetMapping("/user")
    public String userPage() {
        return "user page";
    }

    @GetMapping("/me")
    public User getCurrentUser() {
        User user = (User)SecurityUtils.getSubject().getPrincipal();
        user.setPassword(null);
        return user;
    }

    @RequiresPermissions("user:delete")
    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id) ? "success" : "failed";
    }
}
