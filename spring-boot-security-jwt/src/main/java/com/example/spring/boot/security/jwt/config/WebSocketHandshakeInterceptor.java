package com.example.spring.boot.security.jwt.config;

import com.example.spring.boot.security.jwt.service.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.Optional;

public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired JwtTokenProvider tokenProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest req = (ServletServerHttpRequest) request;
            //从query参数中获取token并解析
            Optional<String> username = Optional.ofNullable(req.getServletRequest().getParameter("token"))
                    .map(tokenProvider::decode);
            if (username.isPresent()) {
                attributes.put("username", username.get());
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {

    }
}
