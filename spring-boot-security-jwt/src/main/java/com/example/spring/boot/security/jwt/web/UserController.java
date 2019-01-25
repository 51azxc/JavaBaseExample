package com.example.spring.boot.security.jwt.web;

import com.example.spring.boot.security.jwt.domain.RoleType;
import com.example.spring.boot.security.jwt.domain.User;
import com.example.spring.boot.security.jwt.dto.LoginRequest;
import com.example.spring.boot.security.jwt.exception.UserExistsException;
import com.example.spring.boot.security.jwt.exception.UserNotFoundException;
import com.example.spring.boot.security.jwt.service.JwtTokenProvider;
import com.example.spring.boot.security.jwt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("users")
public class UserController {

    @Autowired UserService userService;
    @Autowired AuthenticationManager authenticationManager;
    @Autowired JwtTokenProvider tokenProvider;

    @PostMapping("register")
    public HttpEntity<?> signUp(@Valid @RequestBody LoginRequest loginRequest) {
        if (userService.existsUser(loginRequest.getUsername())) {
            throw new UserExistsException("Username: " + loginRequest.getUsername() + " Exists!");
        }
        User u = new User();
        u.setUsername(loginRequest.getUsername());
        u.setPassword(loginRequest.getPassword());
        userService.saveUser(u, Arrays.asList(RoleType.ROLE_USER));
        return ResponseEntity.ok("success");
    }

    @PostMapping("login")
    public HttpEntity<?> signIn(@Valid @RequestBody LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.encode(authentication);
        Map<String, String> map = new HashMap<>();
        map.put("type","Bearer");
        map.put("token", token);
        return ResponseEntity.ok(map);
    }

    @RolesAllowed("ROLE_USER")
    @GetMapping("me")
    public HttpEntity<?> getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails)auth.getPrincipal();
        User u = userService.getUser(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User " + userDetails.getUsername() + " Not Found"));
        return ResponseEntity.ok(u);
    }

    @RolesAllowed("ROLE_ADMIN")
    @GetMapping()
    public HttpEntity<?> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

}
