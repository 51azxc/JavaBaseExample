package com.example.spring.boot.security.jwt.component;

import com.example.spring.boot.security.jwt.dto.UserPrincipal;
import com.example.spring.boot.security.jwt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

public class WebSocketHandshakeHandler extends DefaultHandshakeHandler {

    @Autowired
    UserService userService;

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        Long userId = (Long)attributes.get("userId");
        Optional<String> username = userService.getUsernameById(userId);
        if (username.isPresent()) {
            return new UserPrincipal(username.get());
        }
        return super.determineUser(request, wsHandler, attributes);
    }

}
